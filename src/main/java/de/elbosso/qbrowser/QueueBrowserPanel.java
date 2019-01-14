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
import de.elbosso.model.table.JMSMessageCollectionModel;
import de.elbosso.util.validator.RuleSet;
import de.netsysit.util.validator.Rule;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.util.Collections;

public class QueueBrowserPanel extends javax.swing.JPanel implements de.elbosso.util.pattern.command.RefreshAction.Refreshable
	,javax.swing.event.ListSelectionListener
	,java.awt.event.MouseListener
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(QueueBrowserPanel.class);
	private final static org.apache.log4j.Logger EXCEPTION_LOGGER = org.apache.log4j.Logger.getLogger("ExceptionCatcher");
	private final javax.jms.Session session;
	private final JSpinner textMessageNumberSpinner;
	private de.elbosso.model.table.JMSMessageCollectionModel model;
	private de.netsysit.ui.components.SophisticatedRenderingTable table;
	private final javax.swing.JToolBar toolbar;
	private de.netsysit.model.table.TableSorter sorter;
	private javax.swing.Action refreshAction;
	private javax.swing.Action configFilterAction;
	private javax.swing.Action deleteAction;
	private javax.swing.Action manageFilterAction;
	private javax.swing.Action generateTextMessageAction;
	private java.lang.String destinationName;
	private de.netsysit.ui.dialog.GeneralPurposeOkCancelDialog gpocd;
	private QueueBrowserConfigPanel queueBrowserConfigPanel;
	private TextMessageGeneratorPanel textMessageGeneratorPanel;
	private MessageDetailPanel messageDetailPanel;
	protected javax.swing.JPopupMenu headerpopup =new javax.swing.JPopupMenu();
	protected javax.swing.JPopupMenu popup;
	private int viewColumn;
	private de.netsysit.ui.components.RuleManagementPanel<de.elbosso.util.validator.RuleSet> ruleManagementPanel;
	private de.netsysit.ui.dialog.GeneralPurposeInfoDialog gpid;
	private java.util.Map<java.lang.String,RuleSet> ruleSetMap;
	private java.util.Map<java.lang.Class,JMSMessageSelectorFormat[]> class2MsgSelMap;
	private javax.jms.Queue queue;

	public QueueBrowserPanel(javax.jms.Session session,java.lang.String destinationName) throws javax.jms.JMSException
	{
		super(new java.awt.BorderLayout());
		this.session=session;
		this.destinationName=destinationName;
		ruleSetMap=new java.util.HashMap();
		createActions();
		toolbar=new javax.swing.JToolBar();
		toolbar.setFloatable(false);
		add(toolbar, BorderLayout.NORTH);
		toolbar.add(refreshAction);
		toolbar.add(deleteAction);
		toolbar.addSeparator();
		String selector = queueBrowserConfigPanel!=null?queueBrowserConfigPanel.getMessgeSelector():null;
		queue = session.createQueue(destinationName);
		javax.jms.QueueBrowser qb;
		if (selector == null)
		{
			qb = session.createBrowser(queue);
		}
		else
		{
			qb = session.createBrowser(queue, selector);
		}
		model=new de.elbosso.model.table.JMSMessageCollectionModel(Collections.EMPTY_LIST);
		int n = model.load(qb.getEnumeration(),null);
		if(n== JMSMessageCollectionModel.MAXMESSAGESTOFETCH)
		{
			javax.swing.JOptionPane.showMessageDialog(QueueBrowserPanel.this,"More than "+JMSMessageCollectionModel.MAXMESSAGESTOFETCH+" messages available - consider using filters!");
		}
		qb.close();
		table=new de.netsysit.ui.components.SophisticatedRenderingTable();
		sorter=new de.netsysit.model.table.TableSorter(model);
		sorter.addMouseListenerToHeaderInTable(table);
		table.setModel(sorter);
		de.elbosso.ui.renderer.table.SortableTableRenderer tableHeaderRenderer=new de.elbosso.ui.renderer.table.SortableTableRenderer();
		table.getTableHeader().setDefaultRenderer(tableHeaderRenderer);
		java.text.DateFormat df=new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		table.setDefaultRenderer(java.util.Date.class,de.elbosso.ui.renderer.table.DateRenderer.create(df));
		table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		table.getSelectionModel().addListSelectionListener(this);
		table.getTableHeader().addMouseListener(this);
		toolbar.add(table.getRulesBasedHighLighterManagerAction());
		java.awt.GridBagLayout gbl=new java.awt.GridBagLayout();
		java.awt.GridBagConstraints c=new java.awt.GridBagConstraints();
		javax.swing.JPanel p=new javax.swing.JPanel(gbl);
		p.setLayout(gbl);
		c.gridy=0;
		c.gridx=0;
		c.fill=c.BOTH;
		c.weighty=0.5;
		c.weightx=1.0;
		java.awt.Component l=new de.netsysit.ui.components.VerticalComponentResizer(new javax.swing.JScrollPane(table));
		gbl.addLayoutComponent(l, c);
		p.add(l);
		c.weighty=0.5;
		++c.gridy;
		messageDetailPanel=new MessageDetailPanel();
		l=new de.netsysit.ui.components.VerticalComponentResizer(messageDetailPanel);
		gbl.addLayoutComponent(l, c);
		p.add(l);
		++c.gridy;
		add(p);
		toolbar.add(configFilterAction);
		headerpopup.add(manageFilterAction);
		class2MsgSelMap=new java.util.HashMap();
		class2MsgSelMap.put(java.util.Date.class,new JMSMessageSelectorFormat[]{
				new NotOlderThanSelector(),
				new DateAfterSelector(),
				new DateBeforeSelector(),
				new NotNullSelector()
		});
		class2MsgSelMap.put(java.lang.Integer.class,new JMSMessageSelectorFormat[]{
				new FloatingPointMinMaxSelector(),
				new NotNullSelector()
		});
		class2MsgSelMap.put(java.lang.String.class,new JMSMessageSelectorFormat[]{
				new ContainsSubstringSelector(),
				new NotNullSelector()
		});
		class2MsgSelMap.put(java.lang.Boolean.class,new JMSMessageSelectorFormat[]{
				new BooleanFalseSelector(),
				new BooleanTrueSelector(),
				new NotNullSelector()
		});
		toolbar.addSeparator();
		toolbar.add(generateTextMessageAction);
		textMessageNumberSpinner = new JSpinner(new SpinnerNumberModel(1,1, Integer.MAX_VALUE,10));
		toolbar.add(textMessageNumberSpinner);
	}
	private void createActions()
	{
		refreshAction=new de.elbosso.util.pattern.command.RefreshAction(this);
		generateTextMessageAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/elbosso/ressources/gfx/thirdparty/Gnome-colors-fusion-icon2_48.png")))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(textMessageGeneratorPanel==null)
				{
					textMessageGeneratorPanel = new TextMessageGeneratorPanel();
				}
				if (gpocd == null)
				{
					gpocd = de.netsysit.ui.dialog.GeneralPurposeOkCancelDialog.create(QueueBrowserPanel.this, "Configure Filter");
				}
				gpocd.showDialog(textMessageGeneratorPanel);
				if (gpocd.isCancelled() == false)
				{
					try
					{
						javax.jms.MessageProducer producer=session.createProducer(queue);
						for(int i=0;i<((java.lang.Number)textMessageNumberSpinner.getModel().getValue()).intValue();++i)
						{
							TextMessage textMessage = textMessageGeneratorPanel.generate(session);
							try
							{
								if (textMessageGeneratorPanel.getDeliveryDelayInMs() > 0)
									producer.setDeliveryDelay(textMessageGeneratorPanel.getDeliveryDelayInMs());
							} catch (java.lang.AbstractMethodError err)
							{
								CLASS_LOGGER.warn(err.getMessage(), err);
							}
							CLASS_LOGGER.debug("setTimeToLive "+textMessageGeneratorPanel.getTimeToLiveInMs());
							producer.setTimeToLive(textMessageGeneratorPanel.getTimeToLiveInMs());
							producer.send(textMessage);
						}
					} catch (JMSException exp)
					{
						de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,table,exp);
					}
					refresh();
				}
			}
		};
		configFilterAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Preferences24.gif")))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				if(queueBrowserConfigPanel==null)
				{
					queueBrowserConfigPanel = new QueueBrowserConfigPanel();
				}
				if (gpocd == null)
				{
					gpocd = de.netsysit.ui.dialog.GeneralPurposeOkCancelDialog.create(QueueBrowserPanel.this, "Configure Filter");
				}
				gpocd.showDialog(queueBrowserConfigPanel);
				if (gpocd.isCancelled() == false)
				{
					refresh();
				}
			}
		};
		deleteAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Delete24.gif")))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int[] selectedIndices=table.getSelectedRows();
				try
				{
					//javax.jms.Queue q = session.createQueue(destinationName);
					for (int i : selectedIndices)
					{
						javax.jms.Message msg = model.getMessageAtRow(sorter.getUnsortedIndex(i));
						if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("Deleting msg " + msg.getJMSMessageID());
						javax.jms.MessageConsumer consumer = session.createConsumer(queue, "JMSMessageID = '" + msg.getJMSMessageID() + "'");
						if (CLASS_LOGGER.isDebugEnabled())
							CLASS_LOGGER.debug("selector = " + consumer.getMessageSelector());
						if(consumer!=null)
						{
							javax.jms.Message message = consumer.receive(1000);
							if(message!=null)
							{
								message.acknowledge();
							}
							consumer.close();
						}
					}
				}
				catch(javax.jms.JMSException exp)
				{
					de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,table,exp);
				}
				de.elbosso.util.Utilities.performAction(QueueBrowserPanel.this,refreshAction);
			}
		};
		deleteAction.setEnabled(false);
		manageFilterAction=new javax.swing.AbstractAction("filter")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				int modelIndex=table.convertColumnIndexToModel(viewColumn);
				if(CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace("managing filter for column "+table.getColumnName(modelIndex));
				if(ruleManagementPanel==null)
					ruleManagementPanel=new de.netsysit.ui.components.RuleManagementPanel(table,table.getColumnName(modelIndex), Collections.<Rule>emptyList());
				if(ruleSetMap.containsKey(table.getColumnName(modelIndex))==false)
					ruleSetMap.put(table.getColumnName(modelIndex),new RuleSet(table.getColumnName(modelIndex)));
				if(class2MsgSelMap.containsKey(table.getColumnClass(modelIndex))==true)
				{
					ruleManagementPanel.showDialog(class2MsgSelMap.get(table.getColumnClass(modelIndex)),ruleSetMap.get(table.getColumnName(modelIndex)));
				}
			}
		};

	}

	@Override
	public void refresh()
	{
		try
		{
			String selector = queueBrowserConfigPanel!=null?queueBrowserConfigPanel.getMessgeSelector():null;
			javax.jms.Queue q = session.createQueue(destinationName);
			javax.jms.QueueBrowser qb;
//			model = new de.elbosso.model.table.JMSMessageCollectionModel(Collections.EMPTY_LIST);
			model.clear();
			java.lang.StringBuffer buf=new java.lang.StringBuffer();
			for(int i=0;i<model.getColumnCount();++i)
			{
				java.lang.String key=model.getColumnName(i);
				if((key.equals("Payload")==false)&&(ruleSetMap.containsKey(key)))
				{
					RuleSet ruleSet=ruleSetMap.get(key);
					Rule[] rules=ruleSet.getRules();
					boolean lastWasOR=false;
					for(Rule rule:rules)
					{
						if(JMSMessageSelectorFormat.class.isAssignableFrom(rule.getClass()))
						{
							JMSMessageSelectorFormat jmsMessageSelectorFormat=(JMSMessageSelectorFormat)rule;
							if(CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace("selector part for "+key+": "+jmsMessageSelectorFormat.toJMSMessageSelector(key));
							if((buf.length()>0)&&(lastWasOR==false))
								buf.append(" AND ");
							buf.append(jmsMessageSelectorFormat.toJMSMessageSelector(key));
							lastWasOR=false;
						}
						else if(de.netsysit.util.validator.rules.SpecialOrRule.class.isAssignableFrom(rule.getClass()))
						{
							buf.append(" OR ");
							lastWasOR=true;
						}
						else if(de.netsysit.util.validator.rules.NegationHelper.class.isAssignableFrom(rule.getClass()))
						{
							de.netsysit.util.validator.rules.NegationHelper nh=(de.netsysit.util.validator.rules.NegationHelper)rule;
							rule=nh.getToBeNegated();
							if(JMSMessageSelectorFormat.class.isAssignableFrom(rule.getClass()))
							{
								JMSMessageSelectorFormat jmsMessageSelectorFormat=(JMSMessageSelectorFormat)rule;
								if(CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace("selector part for "+key+": "+jmsMessageSelectorFormat.toJMSMessageSelector(key));
								if((buf.length()>0)&&(lastWasOR==false))
									buf.append(" AND ");
								buf.append("NOT (");
								buf.append(jmsMessageSelectorFormat.toJMSMessageSelector(key));
								buf.append(")");
								lastWasOR=false;
							}
						}
					}
				}
			}
			if(buf.length()>0)
			{
				if((selector!=null)&&(selector.trim().length()>0))
				{
					buf.append(" AND ");
					buf.append(selector);
				}
				selector=buf.toString();
			}
			if (selector == null)
			{
				qb = session.createBrowser(q);
			}
			else
			{
				if(CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace("selector: "+selector);
				qb = session.createBrowser(q, selector);
			}
			RuleSet ruleSet=ruleSetMap.get("Payload");
			java.lang.Runnable rble=new ModelFiller(ruleSet,qb,model,table);
			table.setEnabled(false);
/*			int n = model.load(qb.getEnumeration(),ruleSet!=null?ruleSet.getRules():null);
			if(n>= 100)
			{
				javax.swing.JOptionPane.showMessageDialog(QueueBrowserPanel.this,"More than "+100+" messages available - consider using filters!");
			}
			qb.close();
			boolean ascending=sorter.isAscending();
			int columnSortedBy=sorter.getColumnSortedBy();
			sorter.setModel(model);
			sorter.sortByColumn(columnSortedBy,ascending);
			table.getParent().invalidate();
			table.getParent().validate();
			table.getParent().doLayout();
			table.getParent().repaint();
*/
			new java.lang.Thread(rble).start();
		}
		catch(javax.jms.JMSException e)
		{
			de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,table,e);
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e)
	{
		if (e.getValueIsAdjusting()==false)
		{
			javax.jms.Message msg= null;
			if(table.getSelectedRowCount() == 1)
			{
				msg=model.getMessageAtRow(sorter.getUnsortedIndex(table.getSelectedRow()));
			}
			messageDetailPanel.update(msg);
			deleteAction.setEnabled(table.getSelectedRowCount()>0);
		}
	}
	@Override
	//Implementation of interface java.awt.event.MouseListener
	public void mouseClicked(java.awt.event.MouseEvent mouseEvent0)
	{
	}
	@Override
	//Implementation of interface java.awt.event.MouseListener
	public void mouseEntered(java.awt.event.MouseEvent mouseEvent0)
	{
	}
	@Override
	//Implementation of interface java.awt.event.MouseListener
	public void mouseExited(java.awt.event.MouseEvent mouseEvent0)
	{
	}
	@Override
	//Implementation of interface java.awt.event.MouseListener
	public void mousePressed(java.awt.event.MouseEvent mouseEvent0)
	{
		javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
		viewColumn = columnModel.getColumnIndexAtX(mouseEvent0.getX());
		if(viewColumn>-1)
		{
			maybeShowPopup(mouseEvent0);
		}
	}
	@Override
	//Implementation of interface java.awt.event.MouseListener
	public void mouseReleased(java.awt.event.MouseEvent mouseEvent0) {
		javax.swing.table.TableColumnModel columnModel = table.getColumnModel();
		viewColumn = columnModel.getColumnIndexAtX(mouseEvent0.getX());
		if(viewColumn>-1)
		{
			maybeShowPopup(mouseEvent0);
		}
	}
	private void maybeShowPopup(java.awt.event.MouseEvent e)
	{
		if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("maybe");
		if (e.getComponent() == table)
		{
			if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("source table");
			if (e.isPopupTrigger())
			{
				if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("is popup");
				if(popup!=null)
				{
					popup.removeAll();
					if(popup.getComponentCount()>0)
					{
						popup.show(e.getComponent(), e.getX(), e.getY());
					}
				}
			}
		}
		else
		{
			if ((e.isPopupTrigger())&&(headerpopup.getComponentCount()>0))
			{
				int modelIndex=table.convertColumnIndexToModel(viewColumn);
				if((class2MsgSelMap.containsKey(table.getColumnClass(modelIndex))==true)&&((table.getColumnName(modelIndex).startsWith("JMS"))||(table.getColumnName(modelIndex).equals("Payload"))))
					headerpopup.show(e.getComponent(),e.getX(), e.getY());
			}
		}
	}
}
