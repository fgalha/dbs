package br.com.fgalha.pocs.dbs.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Fernando Romero Galha
 * 
 * Classe que simula um processo continuo que e repetido de N em N milisegundos.
 * O intervalo pode ser determinado e ate alterado em tempo de execucao pelo 
 * atributo <b>interval</b>.
 * 
 */
public abstract class ContinuousProcessUnit<T> extends ProcessUnit<Boolean> {

	private static final Logger LOG = LogManager.getLogger(ContinuousProcessUnit.class);
			
	private boolean keepRunning = true;
	private long interval = 1000;
	private boolean stopOnError = true;

	public ContinuousProcessUnit(String threadName) {
		super(threadName);
	}

	public ContinuousProcessUnit(String threadName, long interval) {
		super(threadName);
		this.interval = interval;
	}

	public ContinuousProcessUnit(String threadName, long interval, boolean stopOnError) {
		super(threadName);
		this.interval = interval;
		this.stopOnError = stopOnError;
	}	
	
	@Override
	public Boolean process() {
		while(keepRunning) {
			try {
				doIteration();
			} catch (Exception e) {
				if (stopOnError) {
					throw new RuntimeException("Erro no processo continuo " + getName(), e);
				} else {
					LOG.error("Erro no processo continuo, porem nao sera parado ate ser forcado pelo metodo stop()", e);
				}
			}
			sleep(interval);
		}
		LOG.info("Processo continuo " + getName() + " finalizado.");
		return true;
	}

	public abstract void doIteration();

	public long getInterval() {
		return interval;
	}
	
	public void setInterval(long interval) {
		this.interval = interval;
	}
	
	@Override
	public synchronized void stop() {
		LOG.info("Chamado stop(). Processo continuo " + getName() + " ira parar.");
		keepRunning = false;
	}
	
}
