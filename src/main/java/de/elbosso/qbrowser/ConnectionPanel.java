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

import javax.jms.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;

public class ConnectionPanel extends javax.swing.JPanel implements java.lang.AutoCloseable
,javax.swing.event.TreeSelectionListener
	,de.elbosso.util.pattern.command.RefreshAction.Refreshable
{
	private final static org.slf4j.Logger CLASS_LOGGER = org.slf4j.LoggerFactory.getLogger(ConnectionPanel.class);
	private final static org.slf4j.Logger EXCEPTION_LOGGER = org.slf4j.LoggerFactory.getLogger("ExceptionCatcher");
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
						if (CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace(java.util.Objects.toString(paths[paths.length - 1].getLastPathComponent()));
						if (CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace(java.util.Objects.toString(paths[paths.length - 1].getLastPathComponent().getClass()));
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
	}
	@Override
	public void close() throws Exception
	{
		session.close();
		connection.close();
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
				if (CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace(java.util.Objects.toString(paths[paths.length - 1].getLastPathComponent()));
				if (CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace(java.util.Objects.toString(paths[paths.length - 1].getLastPathComponent().getClass()));
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
