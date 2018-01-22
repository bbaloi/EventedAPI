package org.mule.modules.eventedapi.messaging;

import java.util.Collection;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.qpid.jms.JmsConnectionFactory;

import org.mule.modules.eventedapi.EventedApiConnector;
import org.mule.modules.eventedapi.util.MessagingConstants;
import org.mule.modules.eventedapi.vo.ConnectionVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class amqpSolaceTransport extends MessagingTransport
{
	
	private ConnectionFactory factory;
	private Connection connection;
	private Session session;
	private Destination destination;
	private MessageProducer producer;
	private MessageConsumer consumer;
	
	private EventedApiConnector _connector;
	private static Logger logger =  LoggerFactory.getLogger(amqpSolaceTransport.class);
	
	public amqpSolaceTransport(String pTransportId,String pTransportName, String pTrasnportType, String pSubjectName, String pPattern,ConnectionVO pConnection) {
		super(pTransportId,pTransportName, pTrasnportType, pSubjectName, pPattern, pConnection);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	public Collection getLatestEvents() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected void init() {
		
		try
		{
			String url = "amqp://"+host+":"+port;
			
			logger.info("AMQP Solace: Creating Conection to : "+url);
			
			factory = new JmsConnectionFactory(user, password, url);
			connection = factory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			
			if(pattern.equals(MessagingConstants.p2p))
				destination = session.createQueue(subject+".amqp");
			if (pattern.equals(MessagingConstants.pub_sub))
				destination = session.createTopic(subject+".amqp");
				
			producer = session.createProducer(destination);
			consumer = session.createConsumer(destination);

			connection.start();

			//logger.info("---Consumer : "+consumer);
	
		} 
		catch (Exception excp1)
		{
			excp1.printStackTrace();
		}
				
	}
	
	public int publishEvent(Event pEvent) throws Exception
	{
		
		//ObjectMessage message = session.createObjectMessage();
		//message.setObject((String)pEvent.getMessagePayload());
		TextMessage message = session.createTextMessage();
		message.setText((String) pEvent.getMessagePayload());
		//System.out.println("Sent: " + message.getText());
		producer.send(message);
		
		logger.info("AMQP Solace sent message:subject="+subject+"pattern="+pattern+", payload="+pEvent.getMessagePayload().toString());
		//client.disconnect();
		
		return 0;
	}
	public void registerConsumer(ICallback pConsumer) throws Exception
	{	
		logger.info("Callback:"+pConsumer+", Consumer:"+consumer);
		consumer.setMessageListener( (MessageListener) pConsumer);
		
	}
	public Event getNextEvent()
	{
		return null;
	}


	public MessageConsumer getConsumer() {
		return consumer;
	}


}
