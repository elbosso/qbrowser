package de.elbosso.qbrowser;
/*
Copyright (c) 2012-2018.
Juergen Key. Alle Rechte vorbehalten.
Weiterverbreitung und Verwendung in nichtkompilierter oder kompilierter Form,
mit oder ohne Veraenderung, sind unter den folgenden Bedingungen zulaessig:
   1. Weiterverbreitete nichtkompilierte Exemplare muessen das obige Copyright,
die Liste der Bedingungen und den folgenden Haftungsausschluss im Quelltext
enthalten.
   2. Weiterverbreitete kompilierte Exemplare muessen das obige Copyright,
die Liste der Bedingungen und den folgenden Haftungsausschluss in der
Dokumentation und/oder anderen Materialien, die mit dem Exemplar verbreitet
werden, enthalten.
   3. Weder der Name des Autors noch die Namen der Beitragsleistenden
duerfen zum Kennzeichnen oder Bewerben von Produkten, die von dieser Software
abgeleitet wurden, ohne spezielle vorherige schriftliche Genehmigung verwendet
werden.
DIESE SOFTWARE WIRD VOM AUTOR UND DEN BEITRAGSLEISTENDEN OHNE
JEGLICHE SPEZIELLE ODER IMPLIZIERTE GARANTIEN ZUR VERFUEGUNG GESTELLT, DIE
UNTER ANDEREM EINSCHLIESSEN: DIE IMPLIZIERTE GARANTIE DER VERWENDBARKEIT DER
SOFTWARE FUER EINEN BESTIMMTEN ZWECK. AUF KEINEN FALL IST DER AUTOR
ODER DIE BEITRAGSLEISTENDEN FUER IRGENDWELCHE DIREKTEN, INDIREKTEN,
ZUFAELLIGEN, SPEZIELLEN, BEISPIELHAFTEN ODER FOLGENDEN SCHAEDEN (UNTER ANDEREM
VERSCHAFFEN VON ERSATZGUETERN ODER -DIENSTLEISTUNGEN; EINSCHRAENKUNG DER
NUTZUNGSFAEHIGKEIT; VERLUST VON NUTZUNGSFAEHIGKEIT; DATEN; PROFIT ODER
GESCHAEFTSUNTERBRECHUNG), WIE AUCH IMMER VERURSACHT UND UNTER WELCHER
VERPFLICHTUNG AUCH IMMER, OB IN VERTRAG, STRIKTER VERPFLICHTUNG ODER
UNERLAUBTE HANDLUNG (INKLUSIVE FAHRLAESSIGKEIT) VERANTWORTLICH, AUF WELCHEM
WEG SIE AUCH IMMER DURCH DIE BENUTZUNG DIESER SOFTWARE ENTSTANDEN SIND, SOGAR,
WENN SIE AUF DIE MOEGLICHKEIT EINES SOLCHEN SCHADENS HINGEWIESEN WORDEN SIND.
 */
import javax.swing.tree.DefaultMutableTreeNode;

public class ConnectionPanelSupport extends java.lang.Object
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(ConnectionPanelSupport.class);
	private final static org.apache.log4j.Logger EXCEPTION_LOGGER = org.apache.log4j.Logger.getLogger("ExceptionCatcher");

	static void buildAndManageTreeModel(ConnectionPanel connectionPanel) throws java.lang.Exception
	{
		javax.jms.ConnectionMetaData connectionMetaData=connectionPanel.connection.getMetaData();
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getJMSProviderName() "+connectionMetaData.getJMSProviderName());
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getProviderMajorVersion() "+connectionMetaData.getProviderMajorVersion());
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getProviderMinorVersion() "+connectionMetaData.getProviderMinorVersion());
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getProviderVersion() "+connectionMetaData.getProviderVersion());
		java.util.Enumeration en=connectionMetaData.getJMSXPropertyNames();
		while(en.hasMoreElements())
		{
			java.lang.Object ref = en.nextElement();
			if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getJMSXPropertyNames() "+ref);
		}
		if(connectionMetaData.getJMSProviderName().equals("ActiveMQ"))
		{
			if(connectionMetaData.getProviderVersion()!=null)
			{
				if (connectionMetaData.getProviderVersion().equals("2.4.0"))
					buildAndManageTreeModelArtemis2x(connectionPanel);
				else if (connectionMetaData.getProviderVersion().equals("5.15.2"))
					buildAndManageTreeModelActiveMQ5x(connectionPanel);
				else
					throw new java.lang.IllegalArgumentException("JMS-Provider not supported! (" + connectionMetaData.getJMSProviderName() + " " + connectionMetaData.getProviderVersion() + ")");
			}
			else
			{
				if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getJMSProviderName() "+connectionPanel.connection.getClass());
				if(connectionPanel.connection.getClass().getName().equals("org.apache.activemq.ActiveMQConnection"))
					buildAndManageTreeModelActiveMQ5x(connectionPanel);
				else
					throw new java.lang.IllegalArgumentException("JMS-Provider not supported! (" + connectionMetaData.getJMSProviderName() + " " + connectionMetaData.getProviderVersion() + ")");

			}
		}
		else
			throw new java.lang.IllegalArgumentException("JMS-Provider not supported! ("+connectionMetaData.getJMSProviderName()+" "+connectionMetaData.getProviderVersion()+")");
	}
	private static void buildAndManageTreeModelArtemis2x(ConnectionPanel connectionPanel) throws java.lang.Exception
	{
		javax.jms.QueueSession session = ((org.apache.activemq.artemis.jms.client.ActiveMQConnection)connectionPanel.connection).createQueueSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		javax.jms.Queue managementQueue = org.apache.activemq.artemis.api.jms.ActiveMQJMSClient.createQueue("activemq.management");
		javax.jms.QueueRequestor requestor = new javax.jms.QueueRequestor(session, managementQueue);
		javax.jms.Message m = session.createMessage();
		org.apache.activemq.artemis.api.jms.management.JMSManagementHelper.putAttribute(m, org.apache.activemq.artemis.api.core.management.ResourceNames.BROKER, "queueNames");
		javax.jms.Message reply = requestor.request(m);
		Object[] queueNames = (Object[]) org.apache.activemq.artemis.api.jms.management.JMSManagementHelper.getResult(reply);
		for (Object queueName : queueNames)
		{
			System.out.println("Queue name: " + queueName+" "+(queueName.getClass()));
			javax.jms.Queue queueInQuestion = org.apache.activemq.artemis.api.jms.ActiveMQJMSClient.createQueue(queueName.toString());
			if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(queueInQuestion);
			javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(queueInQuestion);
			connectionPanel.treeModel.insertNodeInto(node,connectionPanel.queueNode,connectionPanel.queueNode.getChildCount());
		}
	}
	private static void buildAndManageTreeModelActiveMQ5x(final ConnectionPanel connectionPanel) throws java.lang.Exception
	{
		org.apache.activemq.advisory.DestinationSource destinationSource = new org.apache.activemq.advisory.DestinationSource(connectionPanel.connection);
		destinationSource.start();
		final java.util.Set<org.apache.activemq.command.ActiveMQQueue> queues = destinationSource.getQueues();

		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(queues.size());
		for (Object object : queues)
		{
			if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(object);
			javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(object);
			connectionPanel.treeModel.insertNodeInto(node,connectionPanel.queueNode,connectionPanel.queueNode.getChildCount());
		}
		java.util.Set<org.apache.activemq.command.ActiveMQTempQueue> tqueues = destinationSource.getTemporaryQueues();
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(tqueues.size());
		for (Object object : tqueues)
		{
			if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(object);
			javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(object);
			connectionPanel.treeModel.insertNodeInto(node,connectionPanel.tqueueNode,connectionPanel.tqueueNode.getChildCount());
		}
		java.util.Set<org.apache.activemq.command.ActiveMQTopic> topics = destinationSource.getTopics();
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(topics.size());
		for (Object object : topics)
		{
			if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(object);
			javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(object);
			connectionPanel.treeModel.insertNodeInto(node,connectionPanel.topicNode,connectionPanel.topicNode.getChildCount());
		}
		destinationSource.setDestinationListener(new org.apache.activemq.advisory.DestinationListener()
		{
			public void onDestinationEvent(org.apache.activemq.advisory.DestinationEvent event)
			{
				org.apache.activemq.command.ActiveMQDestination d = event.getDestination();
				if (d != null)
				{
					if (org.apache.activemq.command.ActiveMQQueue.class.isAssignableFrom(d.getClass()))
					{
						if (event.isAddOperation())
						{
							if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(d+" added");
							javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(d);
							connectionPanel.treeModel.insertNodeInto(node,connectionPanel.queueNode,connectionPanel.queueNode.getChildCount());
							connectionPanel.tree.repaint();
						}
						else
						{
							if (event.isRemoveOperation())
							{
								if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(d+" removed");
							}
						}
					}
					else if (org.apache.activemq.command.ActiveMQTempQueue.class.isAssignableFrom(d.getClass()))
					{
						if (event.isAddOperation())
						{
							if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(d+" added");
							javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(d);
							connectionPanel.treeModel.insertNodeInto(node,connectionPanel.tqueueNode,connectionPanel.tqueueNode.getChildCount());
						}
						else
						{
							if (event.isRemoveOperation())
							{
								if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(d+" removed");
							}
						}
					}
					else if (org.apache.activemq.command.ActiveMQTopic.class.isAssignableFrom(d.getClass()))
					{
						if (event.isAddOperation())
						{
							if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(d+" added");
							javax.swing.tree.DefaultMutableTreeNode node=new javax.swing.tree.DefaultMutableTreeNode(d);
							connectionPanel.treeModel.insertNodeInto(node,connectionPanel.topicNode,connectionPanel.topicNode.getChildCount());
						}
						else
						{
							if (event.isRemoveOperation())
							{
								if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(d+" removed");
							}
						}
					}
				}
			}
		});
	}
	static boolean manageNodeSelection(javax.swing.tree.DefaultMutableTreeNode selectedNode,ConnectionPanel connectionPanel) throws javax.jms.JMSException
	{
		javax.jms.ConnectionMetaData connectionMetaData = connectionPanel.connection.getMetaData();
		if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
			CLASS_LOGGER.trace("getJMSProviderName() " + connectionMetaData.getJMSProviderName());
		if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
			CLASS_LOGGER.trace("getProviderMajorVersion() " + connectionMetaData.getProviderMajorVersion());
		if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
			CLASS_LOGGER.trace("getProviderMinorVersion() " + connectionMetaData.getProviderMinorVersion());
		if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
			CLASS_LOGGER.trace("getProviderVersion() " + connectionMetaData.getProviderVersion());
		java.util.Enumeration en = connectionMetaData.getJMSXPropertyNames();
		while (en.hasMoreElements())
		{
			java.lang.Object ref = en.nextElement();
			if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
				CLASS_LOGGER.trace("getJMSXPropertyNames() " + ref);
		}
		if (connectionMetaData.getJMSProviderName().equals("ActiveMQ"))
		{
			if (connectionMetaData.getProviderVersion() != null)
			{
				if (connectionMetaData.getProviderVersion().equals("2.4.0"))
					return manageNodeSelectionArtemis2x(selectedNode, connectionPanel);
				else if (connectionMetaData.getProviderVersion().equals("5.15.2"))
					return manageNodeSelectionActiveMQ5x(selectedNode, connectionPanel);
				else
					throw new java.lang.IllegalArgumentException("JMS-Provider not supported! (" + connectionMetaData.getJMSProviderName() + " " + connectionMetaData.getProviderVersion() + ")");
			}
			else
			{
				if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
					CLASS_LOGGER.trace("getJMSProviderName() " + connectionPanel.connection.getClass());
				if (connectionPanel.connection.getClass().getName().equals("org.apache.activemq.ActiveMQConnection"))
					return manageNodeSelectionActiveMQ5x(selectedNode, connectionPanel);
				else
					throw new java.lang.IllegalArgumentException("JMS-Provider not supported! (" + connectionMetaData.getJMSProviderName() + " " + connectionMetaData.getProviderVersion() + ")");

			}
		}
		else
			throw new java.lang.IllegalArgumentException("JMS-Provider not supported! (" + connectionMetaData.getJMSProviderName() + " " + connectionMetaData.getProviderVersion() + ")");
	}

	private static boolean manageNodeSelectionArtemis2x(DefaultMutableTreeNode selectedNode, ConnectionPanel connectionPanel)
	{
		boolean rv=false;
		java.lang.Object ref=selectedNode.getUserObject();
		if (org.apache.activemq.artemis.jms.client.ActiveMQQueue.class.isAssignableFrom(ref.getClass()))
		{
			rv = true;
		}
		else if (org.apache.activemq.artemis.jms.client.ActiveMQTopic.class.isAssignableFrom(ref.getClass()))
		{
			rv = false;
		}
		else
		{
			if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.WARN))CLASS_LOGGER.warn(ref.getClass());
		}
		return rv;
	}
	private static boolean manageNodeSelectionActiveMQ5x(DefaultMutableTreeNode selectedNode, ConnectionPanel connectionPanel)
	{
		boolean rv=false;
		java.lang.Object ref=selectedNode.getUserObject();
		if (org.apache.activemq.command.ActiveMQQueue.class.isAssignableFrom(ref.getClass()))
		{
			rv = true;
		}
		else if (org.apache.activemq.command.ActiveMQTempQueue.class.isAssignableFrom(ref.getClass()))
		{
			rv = true;
		}
		else if (org.apache.activemq.command.ActiveMQTopic.class.isAssignableFrom(ref.getClass()))
		{
			rv = false;
		}
		else
		{
			rv=false;
			if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.WARN))CLASS_LOGGER.warn(ref.getClass());
		}
		return rv;
	}
	static java.lang.String getDestinationName(DefaultMutableTreeNode selectedNode, ConnectionPanel connectionPanel) throws javax.jms.JMSException
	{
		javax.jms.ConnectionMetaData connectionMetaData=connectionPanel.connection.getMetaData();
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getJMSProviderName() "+connectionMetaData.getJMSProviderName());
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getProviderMajorVersion() "+connectionMetaData.getProviderMajorVersion());
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getProviderMinorVersion() "+connectionMetaData.getProviderMinorVersion());
		if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getProviderVersion() "+connectionMetaData.getProviderVersion());
		java.util.Enumeration en=connectionMetaData.getJMSXPropertyNames();
		while(en.hasMoreElements())
		{
			java.lang.Object ref = en.nextElement();
			if(CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace("getJMSXPropertyNames() "+ref);
		}
		if(connectionMetaData.getJMSProviderName().equals("ActiveMQ"))
		{
			if (connectionMetaData.getProviderVersion() != null)
			{
				if(connectionMetaData.getProviderVersion().equals("2.4.0"))
					return getDestinationNameArtemis2x(selectedNode, connectionPanel);
				else if(connectionMetaData.getProviderVersion().equals("5.15.2"))
					return getDestinationNameActiveMQ5x(selectedNode, connectionPanel);
				else
					throw new java.lang.IllegalArgumentException("JMS-Provider not supported! ("+connectionMetaData.getJMSProviderName()+" "+connectionMetaData.getProviderVersion()+")");
			}
			else
			{
				if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))
					CLASS_LOGGER.trace("getJMSProviderName() " + connectionPanel.connection.getClass());
				if (connectionPanel.connection.getClass().getName().equals("org.apache.activemq.ActiveMQConnection"))
					return getDestinationNameActiveMQ5x(selectedNode, connectionPanel);
				else
					throw new java.lang.IllegalArgumentException("JMS-Provider not supported! (" + connectionMetaData.getJMSProviderName() + " " + connectionMetaData.getProviderVersion() + ")");

			}
		}
		else
			throw new java.lang.IllegalArgumentException("JMS-Provider not supported! ("+connectionMetaData.getJMSProviderName()+" "+connectionMetaData.getProviderVersion()+")");
	}
	private static java.lang.String getDestinationNameArtemis2x(DefaultMutableTreeNode selectedNode, ConnectionPanel connectionPanel)
	{
		java.lang.String rv=null;
		java.lang.Object ref=selectedNode.getUserObject();
		org.apache.activemq.artemis.jms.client.ActiveMQDestination destination=null;
		if (org.apache.activemq.artemis.jms.client.ActiveMQQueue.class.isAssignableFrom(ref.getClass()))
		{
			destination=(org.apache.activemq.artemis.jms.client.ActiveMQDestination)ref;
			rv=((org.apache.activemq.artemis.jms.client.ActiveMQQueue)destination).getQueueName();
		}
		return rv;
	}
	private static java.lang.String getDestinationNameActiveMQ5x(DefaultMutableTreeNode selectedNode, ConnectionPanel connectionPanel)
	{
		java.lang.String rv=null;
		java.lang.Object ref=selectedNode.getUserObject();
		org.apache.activemq.command.ActiveMQDestination destination=null;
		if (org.apache.activemq.command.ActiveMQQueue.class.isAssignableFrom(ref.getClass()))
		{
			destination=(org.apache.activemq.command.ActiveMQDestination)ref;
		}
		else if (org.apache.activemq.command.ActiveMQTempQueue.class.isAssignableFrom(ref.getClass()))
		{
			destination=(org.apache.activemq.command.ActiveMQDestination)ref;
		}
		else if (org.apache.activemq.command.ActiveMQTopic.class.isAssignableFrom(ref.getClass()))
		{
			destination=(org.apache.activemq.command.ActiveMQDestination)ref;
		}
		if(destination!=null)
		{
			java.lang.String qname = destination.toString();
			java.lang.String tname = destination.toString();
			if (org.apache.activemq.command.ActiveMQQueue.class.isAssignableFrom(destination.getClass()))
			{
				qname = ((org.apache.activemq.command.ActiveMQQueue) destination).getPhysicalName();
			}
			else if(org.apache.activemq.command.ActiveMQTopic.class.isAssignableFrom(destination.getClass()))
			{
				tname=((org.apache.activemq.command.ActiveMQTopic)destination).getPhysicalName();
				qname=null;
			}
			if(CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace(qname+" "+tname);
			if (qname != null)
			{
				if (qname.startsWith("queue://"))
				{
					qname = qname.substring("queue://".length());
				}
				if (qname.trim().length() > 0)
				{
					rv = qname.trim();
				}
			}
		}
		return rv;
	}
}
