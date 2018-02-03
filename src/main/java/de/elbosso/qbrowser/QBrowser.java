/*
Ursprünglicher Sourcedode, von dem aus die Entwicklung begann:
https://java.net/nonav/projects/mq/sources/mq5/content/mq/src/share/java/examples/applications/qbrowser/QBrowser.java 
*/
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

import javax.naming.Context;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.util.Hashtable;

public class QBrowser extends javax.swing.JFrame implements java.awt.event.WindowListener
{
	private final static org.apache.log4j.Logger CLASS_LOGGER = org.apache.log4j.Logger.getLogger(QBrowser.class);
	private final static org.apache.log4j.Logger EXCEPTION_LOGGER = org.apache.log4j.Logger.getLogger("ExceptionCatcher");
	private javax.swing.JPanel topLevel;
	private javax.swing.JToolBar tb;
	private javax.swing.Action openAction;
	private de.netsysit.util.beans.InterfaceFactory interfaceFactory;
	private de.netsysit.ui.dialog.GeneralPurposeOkCancelDialog gpocd;
	private javax.swing.JTabbedPane tabs;
	private java.util.List<ConnectionPanel> connectionPanels;

	static{
		try
		{
			java.util.Properties iconFallbacks = new java.util.Properties();
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Save24.gif", "device/drawable-mdpi/ic_sd_storage_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/SaveAs24.gif", "toolbarButtonGraphics/general/Save24.gif&action/drawable-mdpi/ic_help_outline_black_36dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Delete16.gif", "action/drawable-mdpi/ic_delete_black_36dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Delete24.gif", "action/drawable-mdpi/ic_delete_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Cut24.gif", "content/drawable-mdpi/ic_content_cut_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Copy24.gif", "content/drawable-mdpi/ic_content_copy_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Paste24.gif", "content/drawable-mdpi/ic_content_paste_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Bookmarks24.gif", "action/drawable-mdpi/ic_bookmark_border_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Find24.gif", "action/drawable-mdpi/ic_search_black_48dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/HighlightSelection24.gif", "content/drawable-mdpi/ic_select_all_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/media/Pause24.gif", "av/drawable-mdpi/ic_pause_circle_outline_black_48dp.png");
//			iconFallbacks.setProperty("toolbarButtonGraphics/media/Pause24.gif", "av/drawable-mdpi/ic_pause_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/navigation/Down24.gif", "navigation/drawable-mdpi/ic_arrow_downward_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/navigation/Up24.gif", "navigation/drawable-mdpi/ic_arrow_upward_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Preferences24.gif","image/drawable-mdpi/ic_tune_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/navigation/Up16.gif","navigation/drawable-mdpi/ic_arrow_upward_black_24dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/navigation/Down16.gif","navigation/drawable-mdpi/ic_arrow_downward_black_24dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Add24.gif","content/drawable-mdpi/ic_add_box_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Remove24.gif","action/drawable-mdpi/ic_delete_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Edit24.gif","editor/drawable-mdpi/ic_border_color_black_48dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/Color24.gif","editor/drawable-mdpi/ic_format_color_fill_black_48dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/Proceed16.gif","navigation/drawable-mdpi/ic_check_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/Cancel16.gif","navigation/drawable-mdpi/ic_cancel_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/verbindung_herstellen3_48.png","action/drawable-mdpi/ic_power_settings_new_black_48dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/Proceed16.gif","navigation/drawable-mdpi/ic_check_black_24dp.png");
//			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/button_x_32.png","navigation/drawable-mdpi/ic_cancel_black_24dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Refresh24.gif","av/drawable-mdpi/ic_replay_black_48dp.png");
			iconFallbacks.setProperty("toolbarButtonGraphics/general/Replace24.gif","action/drawable-mdpi/ic_find_replace_black_48dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/ReplaceAll24.gif","action/drawable-mdpi/ic_find_replace_black_48dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/Template editieren_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/neu_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/elbosso/ressources/gfx/eb/xmlclosingelement_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/ohne_proxy_starten_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/java_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/datenbanktabelle_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/ca/Template editieren_48.png","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/ExpandAll24.gif","de/elbosso/ressources/gfx/eb/material/expand_48.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/CollapseAll24.gif","de/elbosso/ressources/gfx/eb/material/collapse_48.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/ExpandSelected24.gif","de/elbosso/ressources/gfx/eb/material/expand_selected_48.png");
			iconFallbacks.setProperty("de/netsysit/ressources/gfx/common/CollapseSelected24.gif","de/elbosso/ressources/gfx/eb/material/collapse_selected_48.png");
			iconFallbacks.setProperty("de/elbosso/ressources/gfx/eb/queues/queue_new_48.png","action/drawable-mdpi/ic_open_in_new_black_48dp.png");
//			iconFallbacks.setProperty("","toggle/drawable-mdpi/ic_radio_button_unchecked_black_24dp.png");
			de.netsysit.util.ResourceLoader.configure(iconFallbacks);
		}
		catch(java.io.IOException ioexp)
		{
			ioexp.printStackTrace();
		}
	}

	public QBrowser()
	{
		super("QBrowser");
		de.netsysit.util.ResourceLoader.setSize(de.netsysit.util.ResourceLoader.IconSize.small);
		createActions();
		topLevel=new javax.swing.JPanel(new java.awt.BorderLayout());
		setContentPane(topLevel);
		tb=new javax.swing.JToolBar();
		tb.setFloatable(false);
		topLevel.add(tb, BorderLayout.NORTH);
		tb.add(openAction);
		tabs=new javax.swing.JTabbedPane();
		topLevel.add(tabs);
		connectionPanels=new java.util.LinkedList();
		pack();
		setLocation(0,0);
		setSize(1280,1024);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		setVisible(true);
	}
	private void createActions()
	{
		openAction=new javax.swing.AbstractAction(null,new javax.swing.ImageIcon(de.netsysit.util.ResourceLoader.getImgResource("de/netsysit/ressources/gfx/ca/verbindung_herstellen3_48.png")))
		{
			public void actionPerformed(java.awt.event.ActionEvent evt)
			{
				Target target=new Target();
				target.setContextProviderURL("tcp://localhost:61616");
				target.setCredentials(new de.netsysit.ui.dialog.LoginDialog.BasicCredentials("admin","admin"));
				target.setInitialContextFactory("org.apache.activemq.jndi.ActiveMQInitialContextFactory");
				de.netsysit.util.beans.InterfaceFactory interfaceFactory=new de.netsysit.util.beans.InterfaceFactory();
				try
				{
					Component comp = interfaceFactory.fetchInterfaceForBean(target, "JMS Target");
					if (gpocd == null)
						gpocd = de.netsysit.ui.dialog.GeneralPurposeOkCancelDialog.create(QBrowser.this, "JMS Target");
					gpocd.showDialog(comp);
					if (gpocd.isCancelled() == false)
					{
						Hashtable env = new Hashtable();
						env.put(Context.INITIAL_CONTEXT_FACTORY, target.getInitialContextFactory());
						env.put(Context.PROVIDER_URL, target.getContextProviderURL());
						env.put(Context.SECURITY_PRINCIPAL, target.getCredentials().getUserName());
						env.put(Context.SECURITY_CREDENTIALS, target.getCredentials().getPassword());
						javax.naming.Context jndiContext = new javax.naming.InitialContext(env);
						javax.jms.ConnectionFactory connectionFactory = (javax.jms.ConnectionFactory) jndiContext.lookup("ConnectionFactory");
						javax.jms.Connection connection = connectionFactory.createConnection();
						connection.setClientID(QBrowser.class.getName()+"::"+target.getContextProviderURL());
						ConnectionPanel cp=new ConnectionPanel(connection);
						connectionPanels.add(cp);
						tabs.addTab(target.getContextProviderURL(),cp);
					}
				}
				catch(java.lang.Throwable t)
				{
					de.netsysit.util.Utilities.handleException(EXCEPTION_LOGGER,t);
				}
			}
		};
	}
	public static void main(java.lang.String[] args)
	{
		de.elbosso.util.Utilities.configureBasicStdoutLogging(Level.ALL);
		new QBrowser();
	}

	@Override
	public void windowOpened(WindowEvent e)
	{

	}

	@Override
	public void windowClosing(WindowEvent evt)
	{
		for(ConnectionPanel connectionPanel:connectionPanels)
		{
			try
			{
				connectionPanel.close();
			} catch (Exception e)
			{
				EXCEPTION_LOGGER.error(e.getMessage(),e);
			}
		}
		System.exit(0);
	}

	@Override
	public void windowClosed(WindowEvent e)
	{

	}

	@Override
	public void windowIconified(WindowEvent e)
	{

	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{

	}

	@Override
	public void windowActivated(WindowEvent e)
	{

	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{

	}
}