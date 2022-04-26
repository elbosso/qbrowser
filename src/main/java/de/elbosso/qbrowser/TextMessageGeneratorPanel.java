package de.elbosso.qbrowser;

import de.elbosso.ui.components.MultiSequenceFacadeChoosePanel;

import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

public class TextMessageGeneratorPanel extends javax.swing.JPanel
{
	private final static org.slf4j.Logger CLASS_LOGGER = org.slf4j.LoggerFactory.getLogger(TextMessageGeneratorPanel.class);
	private MultiSequenceFacadeChoosePanel<String> multiSequenceFacadeChoosePanel;

	TextMessageGeneratorPanel()
	{
		super(new java.awt.BorderLayout());
		java.util.Map<String,java.lang.Class> itemDescriptions=new java.util.HashMap();
		itemDescriptions.put("payload",java.lang.String.class);
		itemDescriptions.put("JMSType",java.lang.String.class);
		itemDescriptions.put("JMSPriority",java.lang.Integer.class);
		itemDescriptions.put("DeliveryDelay",java.lang.Long.class);
		itemDescriptions.put("TimeToLive",java.lang.Long.class);
		multiSequenceFacadeChoosePanel=new MultiSequenceFacadeChoosePanel(itemDescriptions);
		add(multiSequenceFacadeChoosePanel);

	}
	TextMessage generate(Session session) throws JMSException
	{
		javax.jms.TextMessage message = session.createTextMessage();
		java.lang.Object ref=multiSequenceFacadeChoosePanel.next("payload");
		message.setText(ref!=null?ref.toString():null);
		ref=multiSequenceFacadeChoosePanel.next("JMSType");
		message.setJMSType(ref!=null?ref.toString():null);
		ref=multiSequenceFacadeChoosePanel.next("JMSPriority");
		message.setJMSPriority(ref!=null?(((java.lang.Integer)ref).intValue()):0);
		message.setJMSDeliveryMode(DeliveryMode.PERSISTENT);
		return message;
	}

	public long getTimeToLiveInMs()
	{
		java.lang.Object ref=multiSequenceFacadeChoosePanel.next("TimeToLive");
		CLASS_LOGGER.debug("raw getTimeToLiveInMs "+ref);
		long rv=ref!=null?(((java.lang.Number)ref).longValue()): Long.MAX_VALUE;
		return rv;
	}

	public long getDeliveryDelayInMs()
	{
		java.lang.Object ref=multiSequenceFacadeChoosePanel.next("DeliveryDelay");
		CLASS_LOGGER.debug("raw getDeliveryDelayInMs "+ref);
		long rv=ref!=null?(((java.lang.Number)ref).longValue()):0;
		return rv;
	}
}
