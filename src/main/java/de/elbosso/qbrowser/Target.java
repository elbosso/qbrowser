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
import de.elbosso.util.lang.annotations.BeanInfo;
import de.elbosso.util.lang.annotations.KeyValueStore;
import de.elbosso.util.lang.annotations.Property;
import de.netsysit.ui.dialog.LoginDialog;

@BeanInfo

public class Target extends de.elbosso.util.beans.EventHandlingSupport
{
	private java.lang.String initialContextFactory;
	private java.lang.String contextProviderURL;
	private de.netsysit.ui.dialog.LoginDialog.BasicCredentials credentials;

	@Property
	public LoginDialog.BasicCredentials getCredentials()
	{
		return credentials;
	}

	@Property
	public String getContextProviderURL()
	{
		return contextProviderURL;
	}

	@Property
	public String getInitialContextFactory()
	{
		return initialContextFactory;
	}

	@Property
	public void setContextProviderURL(String contextProviderURL)
	{
		java.lang.String old=getContextProviderURL();
		this.contextProviderURL = contextProviderURL;
		send("contextProviderURL",old,getContextProviderURL());
	}

	@Property
	public void setCredentials(LoginDialog.BasicCredentials credentials)
	{
		de.netsysit.ui.dialog.LoginDialog.BasicCredentials old=getCredentials();
		this.credentials = credentials;
		send("credentials",old,getCredentials());
	}

	@Property(keyValueStore =
	@KeyValueStore(
			key = "de.netsysit.ui.beans.customizerwidgets.java.lang.StringCustomizer.OPTIONS_KEY",
			value="new java.lang.String[]{" +
					"\"org.apache.activemq.jndi.ActiveMQInitialContextFactory\"," +
					"\"org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory\"" +
					"}"
	)
	)
	public void setInitialContextFactory(String initialContextFactory)
	{
		java.lang.String old=getInitialContextFactory();
		this.initialContextFactory = initialContextFactory;
		send("initialContextFactory",old,getInitialContextFactory());
	}
}
