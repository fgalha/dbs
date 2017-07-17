package br.com.fgalha.pocs.dbs.network;

import java.util.List;

/**
 * @author Fernando R Galha
 * 
 * Template para criacao de comandos de entrada do DBS.
 * Nao utilize @Autowired nas classes implementadas, pois ocasionara o eager load do Spring, carregando 
 * muitos dados no servidor.
 * Ao inves disso, utilize context.getBean dentro do metodo execute. 
 *
 */
public interface CommandTemplate {
	
    public int execute(List<String> args);

    public void showHelp();

    public String getCommandName();
    
    public int getMaxSimultaneous();
    
    public int getReturnCode();
    
    public String getResult();
    
    public boolean isAssynchronous();
    
    public boolean isAConsumerWaitStrategy();
    
    public boolean isProducersFinished();

}
