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
import org.apache.log4j.Level;

import javax.jms.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ConnectionPanel extends javax.swing.JPanel implements java.lang.AutoCloseable
,javax.swing.event.TreeSelectionListener
	,de.elbosso.util.pattern.command.RefreshAction.Refreshable
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(ConnectionPanel.class);
	private final static org.apache.log4j.Logger EXCEPTION_LOGGER = org.apache.log4j.Logger.getLogger("ExceptionCatcher");
	final javax.jms.Connection connection;
	private final javax.jms.Session session;
	javax.swing.tree.DefaultTreeModel treeModel;
	final javax.swing.JTree tree;
	javax.swing.tree.DefaultMutableTreeNode queueNode;
	javax.swing.tree.DefaultMutableTreeNode tqueueNode;
	javax.swing.tree.DefaultMutableTreeNode topicNode;
	private de.netsysit.util.pattern.command.CollapseAllTreeAction collapseTreeAction;
	private de.netsysit.util.pattern.command.ExpandAllTreeAction expandTreeAction;
	private de.netsysit.util.pattern.command.CollapseSelectedTreeAction collapseSelectedTreeAction;
	private de.netsysit.util.pattern.command.ExpandSelectedTreeAction expandSelectedTreeAction;
	private javax.swing.Action openQueueBrowserPanelAction;
	private de.elbosso.util.pattern.command.RefreshAction refreshAction;
	private javax.swing.JTabbedPane tabs;
	private java.util.List<QueueBrowserPanel> queueBrowserPanels;
	private javax.swing.Action deleteAction;
	private javax.swing.JToggleButton deletetb;
	private javax.swing.Action countAction;
	private javax.swing.Popup popup;
	private javax.swing.JLabel popupLabel;
	private javax.swing.JProgressBar popupProgress;

	public ConnectionPanel(javax.jms.Connection connection) throws javax.jms.JMSException
	{
		super(new java.awt.BorderLayout());
		this.connection = connection;
		createActions();
		session = connection.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
		connection.start();
		tree=new javax.swing.JTree();
		tree.addTreeSelectionListener(this);
		tree.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		javax.swing.JPanel treePanel=new javax.swing.JPanel(new java.awt.BorderLayout());
		javax.swing.JToolBar toolbar=new javax.swing.JToolBar();
		collapseTreeAction = new de.netsysit.util.pattern.command.CollapseAllTreeAction(tree, null/*i18n.getString("I18NEditor.collapseTreeAction.text")*/, new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/netsysit/ressources/gfx/common/CollapseAll24.gif")));
		expandTreeAction = new de.netsysit.util.pattern.command.ExpandAllTreeAction(tree, null/*i18n.getString("I18NEditor.expandTreeAction.text")*/, new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/netsysit/ressources/gfx/common/ExpandAll24.gif")));
		collapseSelectedTreeAction = new de.netsysit.util.pattern.command.CollapseSelectedTreeAction(tree, null/*i18n.getString("I18NEditor.collapseTreeAction.text")*/, new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/netsysit/ressources/gfx/common/CollapseSelected24.gif")));
		expandSelectedTreeAction = new de.netsysit.util.pattern.command.ExpandSelectedTreeAction(tree, null/*i18n.getString("I18NEditor.expandTreeAction.text")*/, new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/netsysit/ressources/gfx/common/ExpandSelected24.gif")));
		toolbar.setFloatable(false);
		toolbar.add(expandTreeAction);
		toolbar.add(expandSelectedTreeAction);
		toolbar.add(collapseTreeAction);
		toolbar.add(collapseSelectedTreeAction);
		toolbar.addSeparator();
		toolbar.add(refreshAction);
		toolbar.addSeparator();
		toolbar.add(openQueueBrowserPanelAction);
		toolbar.addSeparator();
		toolbar.add(countAction);
		toolbar.addSeparator();
		deletetb=new javax.swing.JToggleButton(deleteAction);
		toolbar.add(deletetb);
		treePanel.add(toolbar, BorderLayout.NORTH);
		treePanel.add(new javax.swing.JScrollPane(tree));
		add(treePanel, BorderLayout.WEST);
		tabs=new javax.swing.JTabbedPane();
		add(tabs);
		queueBrowserPanels=new java.util.LinkedList();
		de.elbosso.util.Utilities.performAction(this,refreshAction);
	}
	private void createActions()
	{
		openQueueBrowserPanelAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/elbosso/ressources/gfx/eb/queues/queue_new_48.png")))
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				javax.swing.tree.TreePath[] paths=tree.getSelectionPaths();
				if(paths!=null)
				{
					if(paths.length>0)
					{
						if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent());
						if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent().getClass());
						if(javax.swing.tree.DefaultMutableTreeNode.class.isAssignableFrom(paths[paths.length - 1].getLastPathComponent().getClass()))
						{
							javax.swing.tree.DefaultMutableTreeNode dmtn=(javax.swing.tree.DefaultMutableTreeNode)paths[paths.length - 1].getLastPathComponent();
							try
							{
								java.lang.String  destinationName=ConnectionPanelSupport.getDestinationName(dmtn,ConnectionPanel.this);
								if(destinationName!=null)
								{
									QueueBrowserPanel queueBrowserPanel = new QueueBrowserPanel(session, destinationName);
									tabs.addTab(dmtn.toString(), queueBrowserPanel);
									queueBrowserPanels.add(queueBrowserPanel);
								}
							}
							catch(javax.jms.JMSException e)
							{
								de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,tree,e);
							}
						}
					}
				}
			}
		};
		openQueueBrowserPanelAction.setEnabled(false);
		refreshAction=new de.elbosso.util.pattern.command.RefreshAction(this);
		deleteAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Delete24.gif")))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(((java.lang.Boolean)deleteAction.getValue(javax.swing.Action.SELECTED_KEY)).booleanValue()==true)
					new CleanMessagesFromQueueThread().start();
			}
		};
		deleteAction.putValue(javax.swing.Action.SELECTED_KEY, java.lang.Boolean.FALSE);
		deleteAction.setEnabled(false);
		countAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("action/drawable-mdpi/ic_info_black_48dp.png")))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				long counter=0;
				javax.swing.tree.TreePath[] paths=tree.getSelectionPaths();
				if(paths!=null)
				{
					if(paths.length>0)
					{
						if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent());
						if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent().getClass());
						if(javax.swing.tree.DefaultMutableTreeNode.class.isAssignableFrom(paths[paths.length - 1].getLastPathComponent().getClass()))
						{
							javax.swing.tree.DefaultMutableTreeNode dmtn=(javax.swing.tree.DefaultMutableTreeNode)paths[paths.length - 1].getLastPathComponent();
							try
							{
								java.lang.String  destinationName=ConnectionPanelSupport.getDestinationName(dmtn,ConnectionPanel.this);
								if(destinationName!=null)
								{
									javax.jms.Queue queue = session.createQueue(destinationName);
									javax.jms.QueueBrowser qb = session.createBrowser(queue);
									java.util.Enumeration en = qb.getEnumeration();
							    while (en.hasMoreElements())
							    {
										try
										{
											javax.jms.Message msg = (javax.jms.Message) en.nextElement();
											if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("Deleting msg " + msg.getJMSMessageID());
//											System.out.println("Deleting msg " + msg.getJMSMessageID());
											++counter;
											if(counter%10000==0)
											{
												if (CLASS_LOGGER.isDebugEnabled())
													CLASS_LOGGER.debug("counting # "+(counter));
												System.out.println("counting # "+(counter));
											}
										}
										catch(javax.jms.JMSException exp)
										{
											de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,tree,exp);
										}
							    }
								}
							}
							catch(javax.jms.JMSException exp)
							{
								de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,tree,exp);
							}
						}
					}
				}
				javax.swing.JOptionPane.showMessageDialog(tree, "At least "+counter+" in queue", "Count Messages", javax.swing.JOptionPane.INFORMATION_MESSAGE);
			}
		};
		countAction.setEnabled(false);
	}
	@Override
	public void close() throws Exception
	{
		session.close();
		connection.close();
	}
	private class CleanMessagesFromQueueThread extends java.lang.Thread
	{
		public void run()
		{
			long counter=0;
			javax.swing.tree.TreePath[] paths=tree.getSelectionPaths();
			if(paths!=null)
			{
				if(paths.length>0)
				{
					if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent());
					if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent().getClass());
					if(javax.swing.tree.DefaultMutableTreeNode.class.isAssignableFrom(paths[paths.length - 1].getLastPathComponent().getClass()))
					{
						javax.swing.tree.DefaultMutableTreeNode dmtn=(javax.swing.tree.DefaultMutableTreeNode)paths[paths.length - 1].getLastPathComponent();
						try
						{
							java.lang.String  destinationName=ConnectionPanelSupport.getDestinationName(dmtn,ConnectionPanel.this);
							if(destinationName!=null)
							{
								javax.jms.Queue queue = session.createQueue(destinationName);
								javax.jms.QueueBrowser qb = session.createBrowser(queue);
								java.util.Enumeration en = qb.getEnumeration();
								javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable()
								{
									@Override
									public void run()
									{
										javax.swing.PopupFactory pf = new javax.swing.PopupFactory(); 
										javax.swing.JPanel p2 = new javax.swing.JPanel(); 
//											popupLabel=new javax.swing.JLabel();
//											popupLabel.setPreferredSize(new java.awt.Dimension(400, 30));
//							        p2.add(popupLabel);
										popupProgress=new javax.swing.JProgressBar();
										popupProgress.setIndeterminate(true);
										popupProgress.setStringPainted(true);
										popupProgress.setPreferredSize(new java.awt.Dimension(400, 55));
										popupProgress.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createEtchedBorder(), "Deletion in Progress..."));
						        p2.add(popupProgress);
						        popup=pf.getPopup(tree, p2, 180, 40); 
//										popupLabel.setText("");
										popupProgress.setString("");
						        popup.show(); 
									}
								});
						    while ((en.hasMoreElements())&&((java.lang.Boolean)deleteAction.getValue(javax.swing.Action.SELECTED_KEY)).booleanValue()==true) 
						    {
									try
									{
										javax.jms.Message msg = (javax.jms.Message) en.nextElement();
										if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("Deleting msg " + msg.getJMSMessageID());
//										System.out.println("Deleting msg " + msg.getJMSMessageID());
										javax.jms.MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '" + msg.getJMSMessageID() + "'");
										if(consumer!=null)
										{
//										System.out.println(consumer);
											if (CLASS_LOGGER.isDebugEnabled())
												CLASS_LOGGER.debug("selector = " + consumer.getMessageSelector());
//										System.out.println("selector = " + consumer.getMessageSelector());
											javax.jms.Message message = consumer.receive(1000);
											++counter;
											if(counter%1000==0)
											{
												if (CLASS_LOGGER.isDebugEnabled())
													CLASS_LOGGER.debug("deleting # "+(counter)+": "+ message);
//												System.out.println("deleting # "+(counter)+": "+message);
												javax.swing.SwingUtilities.invokeLater(new Updater(counter));
											}
											if(message!=null)
											{
												message.acknowledge();
											}
											consumer.close();
										}
									}
									catch(javax.jms.JMSException exp)
									{
										de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,tree,exp);
									}
						    }
								javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable()
								{
									@Override
									public void run()
									{
										popup.hide();
									}
								});
							}
						}
						catch(javax.jms.JMSException exp)
						{
							de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,tree,exp);
						}
					}
				}
			}
		}
	}
	private class Updater extends java.lang.Object implements java.lang.Runnable
	{
		private final long counter;
		
		Updater(long counter)
		{
			super();
			this.counter=counter;
		}
		@Override
		public void run()
		{
//			popupLabel.setText("deleting # "+(counter));
			popupProgress.setString("deleting # "+(counter));
		}
	}
	@Override
	public void valueChanged(TreeSelectionEvent e)
	{
		boolean enabled=false;
		javax.swing.tree.TreePath[] paths=tree.getSelectionPaths();
		if(paths!=null)
		{
			if(paths.length>0)
			{
				if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent());
				if (CLASS_LOGGER.isEnabledFor(org.apache.log4j.Level.TRACE))CLASS_LOGGER.trace(paths[paths.length - 1].getLastPathComponent().getClass());
				if(javax.swing.tree.DefaultMutableTreeNode.class.isAssignableFrom(paths[paths.length - 1].getLastPathComponent().getClass()))
				{
					javax.swing.tree.DefaultMutableTreeNode dmtn=(javax.swing.tree.DefaultMutableTreeNode)paths[paths.length - 1].getLastPathComponent();
					try
					{
						enabled = ConnectionPanelSupport.manageNodeSelection(dmtn, this);
					}
					catch(javax.jms.JMSException exp)
					{
						EXCEPTION_LOGGER.warn(exp.getMessage(),exp);
					}
				}
			}
		}
		openQueueBrowserPanelAction.setEnabled(tree.isEnabled()&&enabled);
		deleteAction.setEnabled(tree.isEnabled()&&enabled);
		countAction.setEnabled(tree.isEnabled()&&enabled);
	}

	@Override
	public void refresh()
	{
		try
		{
			javax.swing.tree.DefaultMutableTreeNode root = new javax.swing.tree.DefaultMutableTreeNode(connection.getClientID());
			treeModel = new javax.swing.tree.DefaultTreeModel(root);
			queueNode = new javax.swing.tree.DefaultMutableTreeNode("Queues");
			tqueueNode = new javax.swing.tree.DefaultMutableTreeNode("TemporaryQueues");
			topicNode = new javax.swing.tree.DefaultMutableTreeNode("Topics");
			root.add(queueNode);
			root.add(tqueueNode);
			root.add(topicNode);
			try
			{
				ConnectionPanelSupport.buildAndManageTreeModel(this);
			} catch (java.lang.Throwable t)
			{
				de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER, tree, t);
			}
			tree.setModel(treeModel);
		}
		catch(javax.jms.JMSException e)
		{
			de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,tree,e);
		}
	}
}
