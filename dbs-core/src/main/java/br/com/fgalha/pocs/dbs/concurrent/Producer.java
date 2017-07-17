/**
 *
 */
package br.com.fgalha.pocs.dbs.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Fernando Romero Galha.
 * @since 19/02/2015
 */
public abstract class Producer<T> extends ProcessUnit<Boolean> {

	private static final Logger LOG = LogManager.getLogger(Producer.class);

    private static final int ONE_MB = 1 * 1024 * 1024;

    private BlockingQueue<T> blockingQueue;

    private boolean keepRunning = true;

    boolean showWarning = true;

    @Value("${rtc.memoryFreeBeforePutQueue:4}")
    private Integer memoryFreeBeforePutQueue;

    @Value("${producer.offer.time:50}")
    private long timeOffer;

    @Value("${producer.offer.timeout:1200000}")
    private long timeout;
    
    @Value("${producer.offer.timeoutWarning:600000}")
    private long timeoutWarning;

    public Producer(String name, BlockingQueue<T> blockingQueue) {
        super(name);
        this.blockingQueue = blockingQueue;
        LOG.debug("Producer " +  getName() + " iniciando.");
    }

    /**
     * Metodo abstrato para produzir itens para a fila, neste metodo deve-se
     * usar o {@link #putInQueue(Object) putInQueue}.
     *
     * @param item
     *            da lista.
     */
    public abstract void produce();

    /**
     * Metodo abstrato para terminar recursos ao fim do processamento.
     */
    @Override
    public abstract void releaseResources();

    /**
     * @param threadName
     */
    public Producer(String threadName) {
        super(threadName);
        throw new IllegalArgumentException("Erro, nao eh possivel instanciar um producer sem uma blocking queue");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.com.bvmf.tem.process.TemProcess#process()
     */
    @Override
    public Boolean process() {
        try {
            produce();
        } finally {
            releaseResources();
        }
        return true;
    }

    /**
     * Enquanto este producer estiver marcado para rodar (flag keepRunning),
     * tentara colocar o objeto na blockingQueue. Se nao houver memoria
     * disponivel para colocar mais objetos na fila, aguarda ate ter memoria
     * para continuar a tentar colocar o objeto na fila.
     * 
     * @param object
     */
    public void putInQueue(T object) {
    	LOG.debug("Producer " +  getName() + " tentara inserir elemento " + object + " na fila");
        while (keepRunning) {
            if (hasMinMemoryAvaiable()) {
                try {
                    boolean success = false;
                    long init = System.currentTimeMillis();
                    while (!success && keepRunning) {
                        success = blockingQueue.offer(object, timeOffer, TimeUnit.MILLISECONDS);
                        if (success) {
                        	LOG.debug("Producer " +  getName() + " inseri elemento " + object + " na com sucesso.");
                        } else {
                        	LOG.debug("Producer " +  getName() + " nao conseguiu inserir o elemento " + object + " na fila.");
                        }
                        if (!success && System.currentTimeMillis() - init > timeoutWarning) {
                        	LOG.warn("Tempo de " + timeoutWarning + " ultrapassado para inserir o objeto: " + object);
                        }
                        if (!success && System.currentTimeMillis() - init > timeout) {
                            throw new IllegalStateException("Erro producer. O Timeout para insercao na fila foi ultrapassado");
                        }
                    }

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
            if (showWarning) {
                LOG.warn("ATENCAO, NAO EXISTE MEMORIA DISPONIVEL PARA O TAMANHO DA FILA.");
                LOG.warn("\tDIMINUA O TAMANHO DA FILA OU AUMENTE A MEMORIA DO PROCESSO JAVA.");
                showWarning = false;
                // System.gc();
            }
            sleep(2000);
        }

    }

    private boolean hasMinMemoryAvaiable() {
        long unusedMemory = Runtime.getRuntime().freeMemory();
        long usedMemory = Runtime.getRuntime().totalMemory() - unusedMemory;
        long memoryLimit = Runtime.getRuntime().maxMemory();
        return (memoryLimit - usedMemory) >= (memoryFreeBeforePutQueue * ONE_MB);
    }

    @Override
    public synchronized void stop() {
        keepRunning = false;
    }

}
