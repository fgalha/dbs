/**
 *
 */
package br.com.b3.digsta.dbs.concurrent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * @author Fernando Romero Galha.
 * @since 12/02/2015
 */
public abstract class ProcessUnit<T> implements Runnable {

	private static final Logger LOGG = LogManager.getLogger(ProcessUnit.class);

    @Autowired
    private ApplicationContext context;
    private String name;
    private Thread currentThread = null;
    private ProcessUnitStatus status = ProcessUnitStatus.READY;
    private boolean finishedProcess = false;
    private Exception exception;
    private T processReturn;

    // flags apenas para controle de erro de logica a ser tratado manualmente
    // no fim de algum processamento
    private String logicalErrorDescription;
    private boolean logicalError = false;

    public abstract T process();

    public void processSynchronous() {
        run();
    }

    /**
     * Executa o thread, chamando o process, logo apos isto, marca o processo
     * como done;
     */
    @Override
    public void run() {
        status = ProcessUnitStatus.RUNNING;
        ProcessUnitStatus auxStatus = status;
        try {
            processReturn = process();
            try {
                onSuccess();
                auxStatus = ProcessUnitStatus.SUCCESS;
            } catch (Exception e) {
                LOGG.error("Erro no onSuccess", e);
                exception = e;
                auxStatus = ProcessUnitStatus.ERROR;
            }
        } catch (Exception e) {
            try {
                onError();
            } catch (Exception e2) {
                LOGG.error("Erro no onError", e);
            }
            LOGG.error("Erro no processo", e);
            exception = e;
            auxStatus = ProcessUnitStatus.ERROR;
        } finally {
            try {
                releaseResources();
            } catch (Exception e2) {
                LOGG.error("Erro no releaseResources", e2);
                exception = e2;
                auxStatus = ProcessUnitStatus.ERROR;
            }
        }
        status = auxStatus;
        finishedProcess = true;
        currentThread = null;
    }

    /**
     * Metodo executado ao obter sucesso no process().
     */
    protected void onSuccess() {
    }

    /**
     * Metodo executado ao obter erro no process().
     */
    protected void onError() {
    }

    /**
     * Metodo para ser sobrescrito caso seja necessario liberar algum recurso no
     * fim do processamento.
     */
    protected void releaseResources() {
    }

    /**
     * Cria uma thread com o nome especificado
     *
     * @param threadName
     *            o nome desta thread
     */
    public ProcessUnit(String threadName) {
        this.name = threadName;
    }

    /**
     * @return Retorna o nome desta Thread.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Inicia a execucao deste objeto
     */
    public synchronized void processAssynchronous() {
        this.currentThread = new Thread(this, getThreadName());
        this.status = ProcessUnitStatus.RUNNING;
        this.currentThread.start();
    }

    /**
     * Inicia a execucao deste objeto como um deamon
     */
    public synchronized void startDaemon() {
        this.currentThread = new Thread(this, getThreadName());
        this.currentThread.setDaemon(true);
        this.currentThread.start();
    }

    /**
     * Para a execucao deste objeto
     */
    public synchronized void stop() {
        if (isRunning()) {
            LOGG.debug(getName() + " esta parando de executar...");
            this.currentThread.interrupt();
            this.currentThread = null;
        }
    }

    /**
     * @return true se o objeto estiver em execucao, caso contrario false
     */
    public synchronized boolean isRunning() {
        return !finishedProcess;
    }

    /**
     * @return true se o objeto tiver terminado sua execucao
     */
    public synchronized boolean isFinished() {
        return finishedProcess;
    }

    /**
     * Para a execucao deste objeto se ele estiver executando
     */
    protected synchronized void stopIfIAmRunning() {
        if (iAmRunning()) {
            stop();
        }
    }

    /**
     * verifica se a thread corrente correponde a este objeto
     *
     * @return <code>true</code> se a thread corrente correponde a este objeto,
     *         caso contrario <code>false</code>
     */
    public synchronized boolean isItMyThread() {
        return this.currentThread == Thread.currentThread();
    }

    /**
     * Identifica se esta thread esta sendo executada.<br>
     * <s>Se o status estiver como nao executando retira esta thread do
     * <code>Monitor</code>.</s><br>
     * <b>Obs:</b> Este metodo pode ser sobrescrito para acrescentar novos
     * comportamentos.
     *
     * @return true se estiver executando, caso contrario false
     */
    protected synchronized boolean iAmRunning() {
        return this.currentThread == Thread.currentThread();
    }

    public String getThreadName() {
        return getName();
    }

    /**
     * Fica aguardando ate este objeto ser parado
     *
     * @throws InterruptedException
     */
    public void join() throws InterruptedException {
        if (this.currentThread != null) {
            LOGG.info("Aguardando a thread " + getName() + " parar de executar...");
            this.currentThread.join();
            LOGG.info("A thread " + getName() + " concluiu sua execucao.");
        }
    }

    /**
     * Retorna o status da execucao.
     *
     * @return
     */
    public ProcessUnitStatus getStatus() {
        return status;
    }

    /**
     * Retorna a excecao, caso ocorra algum erro. O status deste processo devera
     * estar como ERRO.
     *
     * @return
     */
    public Exception getException() {
        return exception;
    }

    public ApplicationContext getContext() {
        return context;
    }

    public void sleep(long miliseconds) {
        try {
            Thread.sleep(miliseconds);
        } catch (InterruptedException e) {
        }
    }

    /**
     * @param i
     */
    public void waitProcessFinish(long milisecondsPeriod) {
        while (isRunning()) {
            sleep(milisecondsPeriod);
        }
    }

    /**
     *
     */
    public T getProcessReturn() {
        return processReturn;
    }

    public void setContext(ApplicationContext context) {
        this.context = context;
    }

    public String getLogicalErrorDescription() {
        return logicalErrorDescription;
    }

    public void setLogicalErrorDescription(String logicalErrorDescription) {
        this.logicalErrorDescription = logicalErrorDescription;
    }

    public boolean isLogicalError() {
        return logicalError;
    }

    public void setLogicalError(boolean logicalError) {
        this.logicalError = logicalError;
    }
}
