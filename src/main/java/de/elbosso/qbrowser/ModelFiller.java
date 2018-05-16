package de.elbosso.qbrowser;

import de.elbosso.model.table.JMSMessageCollectionModel;
import de.elbosso.util.validator.RuleSet;

import javax.jms.JMSException;
import java.util.Collections;
import java.util.Enumeration;

public class ModelFiller extends java.lang.Object implements Runnable
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(ModelFiller.class);
	private final static org.apache.log4j.Logger EXCEPTION_LOGGER = org.apache.log4j.Logger.getLogger("ExceptionCatcher");
	private final de.netsysit.util.validator.Rule[] rules;
	private final Enumeration enumeration;
	private final JMSMessageCollectionModel model;
	private final javax.swing.JTable table;
	private final javax.jms.QueueBrowser qb;
	private final de.netsysit.model.table.TableSorter sorter;
	private final boolean ascending;
	private final int columnSortedBy;

	ModelFiller(RuleSet ruleSet, javax.jms.QueueBrowser qb, JMSMessageCollectionModel model,javax.swing.JTable table,de.netsysit.model.table.TableSorter sorter) throws JMSException
	{
		super();
		this.qb=qb;
		this.enumeration=qb.getEnumeration();
		this.model=model;
		this.rules=ruleSet!=null?ruleSet.getRules():null;
		this.table=table;
		this.sorter=sorter;
		ascending=sorter.isAscending();
		columnSortedBy=sorter.getColumnSortedBy();
		table.setEnabled(false);
		table.setModel(model);
		sorter.setModel(new de.elbosso.model.table.JMSMessageCollectionModel(Collections.EMPTY_LIST));
		
	}
	public void run()
	{
		try
		{
			while ((enumeration.hasMoreElements()) && (model.getRowCount() < model.MAXMESSAGESTOFETCH))
			{
				javax.jms.Message msg = (javax.jms.Message) enumeration.nextElement();
				if ((rules != null) && (rules.length > 0))
				{
					java.lang.String value = null;
					try
					{
						if (msg != null)
						{
							value = msg.toString();
							if (javax.jms.TextMessage.class.isAssignableFrom(msg.getClass()))
							{
								value = ((javax.jms.TextMessage) msg).getText();
							}
						}
						if (CLASS_LOGGER.isTraceEnabled()) CLASS_LOGGER.trace("value: " + value);
						if (CLASS_LOGGER.isTraceEnabled())
							CLASS_LOGGER.trace("validation: " + de.netsysit.util.validator.Utilities.formatFailures(rules, value));
						if (de.netsysit.util.validator.Utilities.formatFailures(rules, value) == null)
						{
							model.addMessage(msg);
						}
					} catch (javax.jms.JMSException exp)
					{
						EXCEPTION_LOGGER.warn(exp.getMessage(), exp);
					}
				}
				else
					model.addMessage(msg);
			}
			qb.close();
		}
		catch(final javax.jms.JMSException exp)
		{
			javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable()
			{
				public void run()
				{
					de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,table,exp);
				}
			});
		}
		javax.swing.SwingUtilities.invokeLater(new java.lang.Runnable()
		{
			public void run()
			{
				sorter.setModel(model);
				sorter.sortByColumn(columnSortedBy,ascending);
				table.setModel(sorter);
				table.setEnabled(true);
			}
		});
	}
}
