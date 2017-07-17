/**
 *
 */
package br.com.b3.digsta.dbs.concurrent;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Classe para gerenciar o inicio de producers e consumers.
 *
 * A flag stopOnError é por padrao true. Portanto se houver erro no producer ou
 * consumer, o processo ira parar quando possivel.
 *
 * @author Fernando Romero Galha.
 * @since 19/02/2015
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class AbstractProducerConsumer<T> extends ProcessUnit<Boolean> {

	private static final Logger LOG = LogManager.getLogger(AbstractProducerConsumer.class);
	
    private List<ProcessUnit> producers = new ArrayList<ProcessUnit>();
    private List<ProcessUnit> consumers = new ArrayList<ProcessUnit>();

    private boolean stopOnError = true;

    protected void afterConsumersFinished() {

    }

    /**
     * @param threadName
     */
    public AbstractProducerConsumer(String name) {
        super(name);
    }

    public void addProducer(ProcessUnit temProcess) {
        producers.add(temProcess);
    }

    public void addConsumer(Consumer<T> temProcess) {
        consumers.add(temProcess);
    }

    /*
     * (non-Javadoc)
     * 
     * @see br.com.bvmf.tem.process.TemProcess#process()
     */
    @Override
    public Boolean process() {

    	LOG.debug("Iniciando controlador producer/consumer");
        try {
        	LOG.debug("Iniciando producers");
            for (ProcessUnit<T> temProcess : producers) {
                temProcess.processAssynchronous();
            }

            LOG.debug("Iniciando consumers");
            for (ProcessUnit<T> temProcess : consumers) {
                temProcess.processAssynchronous();
            }

            LOG.debug("Aguardando termino dos producers (ou erro nos consumers)");
            boolean hasError = false;
            while (!isDone(producers) && !(hasError = isError(consumers))) {
                Thread.sleep(100);
            }

            if (hasError) {
            	LOG.debug("Houve erro, parando todos producers e consumers.");
                stopAll();
            }

            LOG.debug("Enviando sinal aos consumers para pararem de processar caso nao haja elementos na fila.");
            for (ProcessUnit<T> consumer : consumers) {
                Consumer c = (Consumer) consumer;
                c.stopAfterQueueIsEmpty();
            }

            LOG.debug("Aguardando todos consumers terminarem.");
            while (!isDone(consumers)) {
                Thread.sleep(100);
            }

            for (ProcessUnit producer : producers) {
                if (producer.isFinished() && producer.getStatus() == ProcessUnitStatus.ERROR) {
                    throw new IllegalStateException("Erro no producer " + producer.getName());
                }
            }
            for (ProcessUnit consumer : consumers) {
                if (consumer.isFinished() && consumer.getStatus() == ProcessUnitStatus.ERROR) {
                    throw new IllegalStateException("Erro no consumer " + consumer.getName());
                }
            }

            LOG.debug("Chamando afterConsumersFinished.");
            afterConsumersFinished();
            LOG.debug("Fim do processo de producer / consumer.");

        } catch (InterruptedException e) {
            throw new RuntimeException("Erro no producer / consumer", e);
        }

        return true;
    }

    private boolean isError(List<ProcessUnit> listProcess) {
        for (ProcessUnit temProcess : listProcess) {
            if (temProcess.getStatus().equals(ProcessUnitStatus.ERROR)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param listProcess
     * @return
     */
    private boolean isDone(List<? extends ProcessUnit> listProcess) {
        for (ProcessUnit<T> process : listProcess) {
            if (!process.isFinished()) {
                return false;
            } else {
                if (process.getStatus() == ProcessUnitStatus.ERROR && stopOnError) {
                    stopAll();
                }
            }
        }
        return true;
    }

    private synchronized void stopAll() {
        for (ProcessUnit producer : producers) {
            producer.stop();
        }
        for (ProcessUnit consumer : consumers) {
            consumer.stop();
        }
    }

    public List<ProcessUnit> getProducers() {
        return producers;
    }

    public List<ProcessUnit> getConsumers() {
        return consumers;
    }

    public boolean isStopOnError() {
        return stopOnError;
    }

    public void setStopOnError(boolean stopOnError) {
        this.stopOnError = stopOnError;
    }

}
