/**
 *
 */
package br.com.fgalha.pocs.dbs.concurrent;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author Fernando Romero Galha.
 * @since 19/02/2015
 */
public abstract class Consumer<T> extends ProcessUnit<Boolean> {

	private static final Logger LOG = LogManager.getLogger(Consumer.class);
	
    private boolean keepRunning = true;
    private boolean stopIfQueueIsEmpty = false;

    private BlockingQueue<T> blockingQueue;

    private long timeout;
    
    @Value("${consumer.poll.time:1000}")
    private long timePoll;

    public Consumer(String name, BlockingQueue<T> blockingQueue) {
        super(name);
        this.blockingQueue = blockingQueue;
    }

    public Consumer(String name, BlockingQueue<T> blockingQueue, long timeout) {
        super(name);
        this.blockingQueue = blockingQueue;
        this.timeout = timeout;
    }

    @PostConstruct
    public void postConstruct() {
        long minimumTimeout = getContext().getEnvironment().getProperty("consumer.poll.minimumTimeout", Long.class, 60000L);
        if (getContext().getEnvironment().containsProperty("consumer.poll.timeout") && timeout == 0L) {
        	timeout = getContext().getEnvironment().getProperty("consumer.poll.timeout", Long.class);
        } else if (timeout == 0L) {
        	timeout = getContext().getEnvironment().getProperty("consumer.poll.defaultTimeout", Long.class, 600000L);
        }    	
    	if (timeout < minimumTimeout) {
        	throw new IllegalArgumentException("Erro contrutor consumer, o timeout deve ser maior que " + minimumTimeout + " milisegundos ");
        }
    }
    
    
    /**
     * Metodo abstrato para consumir determinado item da lista.
     *
     * @param item
     *            da lista.
     */
    public abstract void consume(T e, int n);

    /**
     * Metodo abstrato para terminar recursos ao fim do processamento.
     */
    @Override
    public abstract void releaseResources();

    /**
     * @param threadName
     */
    public Consumer(String threadName) {
        super(threadName);
        throw new IllegalArgumentException("Erro, nao eh possivel instanciar um consumer sem uma blocking queue");
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.com.bvmf.tem.process.TemProcess#process()
     */
    @Override
    public Boolean process() {
    	LOG.debug("Iniciando consumer " + getName());
        long init = System.currentTimeMillis();
        while (keepRunning) {
            int i = 0;
            try {
            	LOG.debug("Consumer " + getName() + " Tentando buscar elementos na fila");
                T poll = blockingQueue.poll(timePoll, TimeUnit.MILLISECONDS);
                if (poll != null) {
                	LOG.debug("Consumer " + getName() + " encontrou elemento " + poll);
                    consume(poll, i++);
                    LOG.debug("Consumer " + getName() + " processou elemento " + poll);
                    init = System.currentTimeMillis();
                } else {
                	LOG.debug("Consumer " + getName() + " Nao achou elementos na fila");
                    if (stopIfQueueIsEmpty) {
                    	LOG.debug("Consumer " + getName() + " Flag para parar se a fila estiver vazia ATIVA, saindo...");
                        break;
                    }
                    if (System.currentTimeMillis() - init > timeout) {
                        throw new IllegalStateException("Erro consumer. O Timeout para busca na fila foi ultrapassado.");
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return true;
    }

    /**
     * Marca para termino o consumo de filas
     */
    public synchronized void stopAfterQueueIsEmpty() {
        stopIfQueueIsEmpty = true;
    }

    /**
     * Termina imediatamente o consumo da fila. Nao e garantido que todas os
     * itens da fila foram consumidos.
     */
    @Override
    public synchronized void stop() {
        keepRunning = false;
    }

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
}
