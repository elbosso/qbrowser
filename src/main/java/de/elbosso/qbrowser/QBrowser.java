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
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
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
				target.setContextProviderURL("tcp://localhost:8181?httpUpgradeEnabled=true");
				target.setCredentials(new de.netsysit.ui.dialog.LoginDialog.BasicCredentials("jmsjboss","jmsjboss_1"));
				target.setInitialContextFactory("org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory");
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
						env.put("connectionFactory.ConnectionFactory",target.getContextProviderURL());
						if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug("env " + env);
						javax.naming.Context jndiContext = new javax.naming.InitialContext(env);
						NamingEnumeration<NameClassPair> list = jndiContext.list("");
						while (list.hasMore()) {
							NameClassPair nameClassPair=list.next();
							if (CLASS_LOGGER.isDebugEnabled()) CLASS_LOGGER.debug(nameClassPair.getName()+" "+nameClassPair.getClassName());
						}
						javax.jms.ConnectionFactory connectionFactory = (javax.jms.ConnectionFactory) jndiContext.lookup("ConnectionFactory");
						javax.jms.Connection connection = connectionFactory.createConnection(env.get(Context.SECURITY_PRINCIPAL).toString(), env.get(Context.SECURITY_CREDENTIALS).toString());
						connection.setClientID(QBrowser.class.getName()+"::"+target.getContextProviderURL());
						ConnectionPanel cp=new ConnectionPanel(connection);
						connectionPanels.add(cp);
						tabs.addTab(target.getContextProviderURL(),cp);
					}
				}
				catch(java.lang.Throwable t)
				{
					de.elbosso.util.Utilities.handleException(EXCEPTION_LOGGER,t);
				}
			}
		};
	}
	public static void main(java.lang.String[] args)
	{
		try
		{
			java.util.Properties iconFallbacks = new java.util.Properties();
			java.io.InputStream is=de.netsysit.util.ResourceLoader.getResource("de/elbosso/ressources/data/icon_trans_material.properties").openStream();
			iconFallbacks.load(is);
			is.close();
			de.netsysit.util.ResourceLoader.configure(iconFallbacks);
		}
		catch(java.io.IOException ioexp)
		{
			ioexp.printStackTrace();
		}

		de.netsysit.util.ResourceLoader.setSize(de.netsysit.util.ResourceLoader.IconSize.small);
		de.elbosso.util.Utilities.configureBasicStdoutLogging(Level.INFO);
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