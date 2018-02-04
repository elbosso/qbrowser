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
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class MessageDetailPanel extends javax.swing.JPanel implements java.awt.event.ActionListener
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(MessageDetailPanel.class);
	private final static org.apache.log4j.Logger EXCEPTION_LOGGER = org.apache.log4j.Logger.getLogger("ExceptionCatcher");
	private javax.swing.JTable headerTable;
	private javax.swing.JTable propertiesTable;
	private de.netsysit.ui.components.DockingPanel dockingPanel;
	private javax.swing.JPanel payloadPanel;
	private de.netsysit.ui.text.TextEditor textEditor;
	private javax.swing.JComboBox documentsComboBox;
	private final static java.lang.Object[] supportedDocumentTypes=new java.lang.Object[]{
			new de.netsysit.documents.SyntaxDocument(),
			new de.netsysit.documents.XMLSyntaxDocument(),
			new de.netsysit.documents.HTMLSyntaxDocument(),
			new de.netsysit.documents.JavaSyntaxDocument(),
			new de.netsysit.documents.ShellScriptSyntaxDocument(),
			new de.netsysit.documents.SQLSyntaxDocument(),
			new de.netsysit.documents.VelocitySyntaxDocument(),
	};
	private javax.swing.Action inspectBinaryDataAction;
	private de.elbosso.ui.dialog.ByteArrayDialog badialog;//new de.elbosso.ui.dialog.ByteArrayDialog(null,i18n.getString("ViewPanel.inspectBinaryDataAction.dialog.title"),false);
	byte[] underpopupba;

	MessageDetailPanel()
	{
		super(new java.awt.BorderLayout());
		createActions();
		payloadPanel=new javax.swing.JPanel(new java.awt.BorderLayout());
		dockingPanel=new de.netsysit.ui.components.DockingPanel(payloadPanel, SwingConstants.VERTICAL);

		headerTable=new javax.swing.JTable();
		dockingPanel.addDockable(new javax.swing.JScrollPane(headerTable),"Headers");

		propertiesTable=new javax.swing.JTable();
		dockingPanel.addDockable(new javax.swing.JScrollPane(propertiesTable),"Properties");
		add(dockingPanel);
		setMinimumSize(new java.awt.Dimension(500,500));
		dockingPanel.setEnabled(false);
		textEditor=new de.netsysit.ui.text.TextEditor(new de.netsysit.ui.text.AugmentedJEditTextArea());
		payloadPanel.add(textEditor.getTextField());
		textEditor.setEditable(false);
		javax.swing.JToolBar tb=new javax.swing.JToolBar();
		tb.setFloatable(false);
		tb.add(textEditor.getSaveAction());
		tb.add(textEditor.getSaveAsAction());
		tb.addSeparator();
		tb.add(textEditor.getCopyAction());
		tb.addSeparator();
		tb.add(textEditor.getGotoLineNumberAction());
		tb.addSeparator();
		tb.add(textEditor.getFindPreviousAction());
		tb.add(textEditor.getFindAction());
		tb.add(textEditor.getFindNextAction());
		tb.addSeparator();
		tb.add(textEditor.getHighlightSelectionAction());
		tb.addSeparator();
		tb.add(textEditor.getGotoPreviousBookmarkAction());
		tb.add(textEditor.getToggleBookmarkAction());
		tb.add(textEditor.getGotoNextBookmarkAction());
		tb.addSeparator();
		tb.add(inspectBinaryDataAction);
		tb.addSeparator();
		documentsComboBox=new javax.swing.JComboBox(supportedDocumentTypes);
		documentsComboBox.setSelectedIndex(0);
		tb.add(documentsComboBox);
		documentsComboBox.addActionListener(this);
		documentsComboBox.setRenderer(new de.elbosso.ui.renderer.list.IconProviderRenderer());
		payloadPanel.add(tb, BorderLayout.NORTH);
		textEditor.getToggleBookmarkAction().putValue(Action.SMALL_ICON,(de.netsysit.util.ResourceLoader.getIcon("toolbarButtonGraphics/general/Bookmarks24.gif")));
		try
		{
			textEditor.getPasteAfterEraseAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Paste24.gif"), de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Delete16.gif"))));
		}
		catch(java.lang.Throwable exp)
		{
		}
		try
		{
			textEditor.getGotoPreviousBookmarkAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Bookmarks24.gif"), de.netsysit.util.ResourceLoader.getImgResource("navigation/drawable-mdpi/ic_chevron_left_black_36dp.png"))));
		}
		catch(java.lang.Throwable exp)
		{
		}
		try
		{
			textEditor.getGotoNextBookmarkAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Bookmarks24.gif"), de.netsysit.util.ResourceLoader.getImgResource("navigation/drawable-mdpi/ic_chevron_right_black_36dp.png"))));
		}
		catch(java.lang.Throwable exp)
		{
		}
		try
		{
			textEditor.getFindPreviousAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Find24.gif"), de.netsysit.util.ResourceLoader.getImgResource("navigation/drawable-mdpi/ic_chevron_left_black_36dp.png"))));
		}
		catch(java.lang.Throwable exp)
		{
		}
		try
		{
			textEditor.getFindNextAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Find24.gif"), de.netsysit.util.ResourceLoader.getImgResource("navigation/drawable-mdpi/ic_chevron_right_black_36dp.png"))));
		}
		catch(java.lang.Throwable exp)
		{
		}
		textEditor.getSaveAsAction().putValue(Action.SMALL_ICON,de.netsysit.util.ResourceLoader.getIcon("toolbarButtonGraphics/general/SaveAs24.gif"));
/*		try
		{
			ed.getSaveAsAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Save24.gif"), de.netsysit.util.ResourceLoader.getImgResource("action/drawable-mdpi/ic_help_outline_black_36dp.png"))));
		}
		catch(java.lang.Throwable exp)
		{
		}
*/		try
	{
		textEditor.getGotoLineNumberAction().putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.ui.image.DecoratedImageProducer.produceImage(de.netsysit.util.ResourceLoader.getImgResource("action/drawable-mdpi/ic_view_headline_black_48dp.png"), de.netsysit.util.ResourceLoader.getImgResource("action/drawable-mdpi/ic_trending_flat_black_36dp.png"), SwingUtilities.SOUTH_WEST)));
	}
	catch(java.lang.Throwable exp)
	{
	}

	}
	private void createActions()
	{
		inspectBinaryDataAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("toolbarButtonGraphics/general/Zoom24.gif")))
		{;
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				if(underpopupba!=null)
				{
					if(badialog==null)
					{
						java.awt.Component root=javax.swing.SwingUtilities.getRoot(MessageDetailPanel.this);
						if(root!=null)
						{
							if(root instanceof java.awt.Dialog)
								badialog=new de.elbosso.ui.dialog.ByteArrayDialog((java.awt.Dialog)root,"BytesMessage",false,null);
							else
								badialog=new de.elbosso.ui.dialog.ByteArrayDialog((java.awt.Frame)root,"BytesMessage",false,null);
						}
						else
							badialog=new de.elbosso.ui.dialog.ByteArrayDialog((java.awt.Frame)null,"BytesMessage",false,null);
					}
					badialog.setLocationRelativeTo(MessageDetailPanel.this);
					badialog.setSize(new java.awt.Dimension(300,300));
					badialog.showDialog(underpopupba);
					underpopupba=null;
				}
			}
		};
		inspectBinaryDataAction.setEnabled(false);
		inspectBinaryDataAction.putValue(Action.SMALL_ICON,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("action/drawable-mdpi/ic_lightbulb_outline_black_48dp.png")));
	}
	public void update(javax.jms.Message message)
	{
		try
		{
			de.elbosso.model.table.PropertiesTable hpt = new de.elbosso.model.table.PropertiesTable()
			{
				@Override
				public String getColumnName(int columnIndex)
				{
					return columnIndex == 0 ? "Header" : "Value";
				}
			};
			de.elbosso.model.table.PropertiesTable ppt = new de.elbosso.model.table.PropertiesTable();
			if (message != null)
			{
				java.util.Map<java.lang.String, java.lang.String> headerMap = jmsHeadersToHashMap(message);
				if(CLASS_LOGGER.isTraceEnabled())CLASS_LOGGER.trace(headerMap);
				hpt.putAll(headerMap);
				java.util.Map<java.lang.String, java.lang.String> propertiesMap = new java.util.HashMap();
				for (java.util.Enumeration e = message.getPropertyNames();e.hasMoreElements();)
				{
					String name = (e.nextElement()).toString();
					if(message.getObjectProperty(name)!=null)
					propertiesMap.put(name, message.getObjectProperty(name).toString());
				}
				ppt.putAll(propertiesMap);
				underpopupba=null;
				if(javax.jms.TextMessage.class.isAssignableFrom(message.getClass()))
				{
					textEditor.setText(((javax.jms.TextMessage)message).getText());
				}
				if(javax.jms.BytesMessage.class.isAssignableFrom(message.getClass()))
				{
					javax.jms.BytesMessage bm=((javax.jms.BytesMessage)message);
					byte[] buf=new byte[4096];
					java.io.ByteArrayOutputStream baos=new java.io.ByteArrayOutputStream();
					int read=bm.readBytes(buf);
					while(read>-1)
					{
						baos.write(buf,0,read);
						read=bm.readBytes(buf);
					}
					baos.close();
					underpopupba=baos.toByteArray();
				}
				else
					textEditor.setText(message.toString());
				inspectBinaryDataAction.setEnabled(underpopupba!=null);
			}
			else
				textEditor.setText("");
			headerTable.setModel(hpt);
			propertiesTable.setModel(ppt);
			dockingPanel.setEnabled(message!=null);
		}
		catch(java.lang.Throwable t)
		{
			de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,this,t);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		java.lang.String content=textEditor.getText();
		textEditor.setDocument(((org.syntax.jedit.SyntaxDocument)documentsComboBox.getSelectedItem()),false);
		textEditor.setText(content);
	}

	class PropertyPanel extends javax.swing.JPanel
	{

		javax.swing.JLabel label = null;
		javax.swing.JTextArea textArea = null;
		javax.swing.JScrollPane areaScrollPane = null;

		PropertyPanel()
		{
			super(true);
			setBorder(javax.swing.BorderFactory.createEtchedBorder());
			setLayout(new java.awt.BorderLayout());

			label = new javax.swing.JLabel();

			textArea = new javax.swing.JTextArea();
			textArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12));
			textArea.setLineWrap(true);
			textArea.setWrapStyleWord(true);

			areaScrollPane = new javax.swing.JScrollPane(textArea);
			areaScrollPane.setVerticalScrollBarPolicy(
					javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			areaScrollPane.setPreferredSize(new java.awt.Dimension(500, 150));

			add(java.awt.BorderLayout.NORTH, label);
			add(java.awt.BorderLayout.CENTER, areaScrollPane);
		}

		void setTitle(String title)
		{
			label.setText(title);
		}

		/**
		 * Display a HashMap in the text window
		 */
		void load(java.util.HashMap map)
		{

			StringBuffer buf = new StringBuffer();

			java.util.Set entries = map.entrySet();
			java.util.Map.Entry entry = null;
			java.util.Iterator iter = entries.iterator();
			while (iter.hasNext())
			{
				entry = (java.util.Map.Entry) iter.next();
				String key = entry.getKey().toString();

				Object o = entry.getValue();
				String value = "";
				if (o != null)
				{
					value = o.toString();
				}

				buf.append(pad(key + ": ", 20));
				buf.append(value + "\n");
			}

			textArea.setText(buf.toString());

			areaScrollPane.scrollRectToVisible(new java.awt.Rectangle(0, 0, 1, 1));

		}

		/**
		 * Display text in the text window
		 */
		void load(String s)
		{
			textArea.setText(s);
		}

		/**
		 * Pad a string to the specified width, right justifitextEditor. If the string
		 * is longer than the width you get back the original string.
		 */
		String pad(String s, int width)
		{

			// Very inefficient, but we don't care
			StringBuffer sb = new StringBuffer();
			int padding = width - s.length();

			if (padding <= 0)
			{
				return s;
			}

			while (padding > 0)
			{
				sb.append(" ");
				padding--;
			}
			sb.append(s);
			return sb.toString();
		}
	}
	private java.util.Map<java.lang.String,java.lang.String> jmsHeadersToHashMap(javax.jms.Message message) throws javax.jms.JMSException
	{
		java.util.Map<java.lang.String,java.lang.String>rv = new java.util.HashMap();
		String value = message.getJMSCorrelationID();
		rv.put("JMSCorrelationID", value!=null?value:"");

		value = String.valueOf(message.getJMSDeliveryMode());
		rv.put("JMSDeliverMode", value!=null?value:"");

		javax.jms.Destination dest = message.getJMSDestination();
		if (dest != null)
		{
			if (dest instanceof javax.jms.Queue)
			{
				value = ((javax.jms.Queue) dest).getQueueName();
			}
			else if (dest instanceof javax.jms.Topic)
			{
				value = ((javax.jms.Topic) dest).getTopicName();
			}
			else
			{
				value=dest.toString();
			}
		}
		else
		{
			value = "";
		}
		rv.put("JMSDestination", value!=null?value:"");
		rv.put("JMSReplyTo", value!=null?value:"");

		value = String.valueOf(message.getJMSExpiration());
		rv.put("JMSExpiration", value!=null?value:"");

		value = message.getJMSMessageID();
		rv.put("JMSMessageID", value!=null?value:"");

		value = String.valueOf(message.getJMSPriority());
		rv.put("JMSPriority", value!=null?value:"");

		value = String.valueOf(message.getJMSRedelivered());
		rv.put("JMSRedelivered", value!=null?value:"");

		value = String.valueOf(message.getJMSTimestamp());
		rv.put("JMSTimestamp", value!=null?value:"");

		value = message.getJMSType();
		rv.put("JMSType", value!=null?value:"");

		return rv;
	}
}
