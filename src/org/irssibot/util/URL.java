package org.irssibot.util;

import regex2.Matcher;
import regex2.Pattern;

import java.util.*;

import static java.lang.Integer.parseInt;

/**
 * User: parkerkane
 * Date: 9.12.2010
 * Time: 14:50
 */
public class URL {

	public enum Host {NONE, NAME, IPV4, IPV6}

	// domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
	static private final String domainLabelCode =
		"(?<domainlabel>" +
		"  (?:" +
		"    [a-z0-9]" +
		"    [a-z0-9-]*" +
		"    [a-z0-9]" +
		"  )" +
		"  |" +
		"  [a-z0-9]" +
		")";

	// toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
	static private final String topLabelCode =
		"(?<toplabel>" +
		"  (?:" +
		"    [a-z]" +
		"    [a-z0-9-]*" +
		"    [a-z0-9]" +
		"  )" +
		"  |" +
		"  (?:[a-z])" +
		")";

	// hostnameCode      = *( domainlabel "." ) toplabel [ "." ]
	static private final String hostnameCode =
		"(?<hostname>" +
		"  (?:%1$s\\.)*" +                      // var: domainLabelCode
		"  %2$s" +                              // var: topLabelCode
		")";

	static private final String ipv4Code =
		"(?<ipv4>" +
//		"  (?:(?<=[^0-9])|\\A)" +				// Doesn't start with number
		"  (?:[0-9]+\\.){3}[0-9]+" +    // '123.123.123.123'
		"  (?![0-9])" +                         // And doesn't end with number
		")";

	static private final String singleNumCode = "[0-9a-f]{0,4}";

	static private final String ipv6Code      =
		"(?:" +
		"  \\[" +                               // Start [
		"  (?<ipv6>" +
		"    (?:%1$s[:]){2,7}" +                // xxxx:xxxx:[xxxx:[xxxx:[xxxx:[xxxx:[xxxx:]]]]]
		"    %1$s" +                            // xxxx
		"  )" +
		"  \\]" +                               // End ]
		")";

	static private final String hostCode =
		"(?<host>" +
		"  (?:" +                               // Must be one of these"+
		"    %1$s" +                            // var: hostnameCode"+
		"    |%2$s" +                           // var: ipv4Code"+
		"    |%3$s" +                           // var: ipv6Code"+
		"  )" +
		"  (?:" +                               // Check if port is defined"+
		"    :" +
		"    (?<port>" +
		"      [0-9]+" +
		"    )" +
		"  )?" +
		")";

	static private final String[] countryCodeData = {
		"ac",   "ad",   "ae",   "af",     "ag",     "ai",   "al",  "am",   "an",   "ao", "aq", "ar",
		"as",   "at",   "au",   "aw",     "ax",     "az",   "ba",  "bb",   "bd",   "be", "bf", "bg",
		"bh",   "bi",   "bj",   "bm",     "bn",     "bo",   "br",  "bs",   "bt",   "bv", "bw", "by",
		"bz",   "ca",   "cc",   "cd",     "cf",     "cg",   "ch",  "ci",   "ck",   "cl", "cm", "cn",
		"co",   "cr",   "cu",   "cv",     "cx",     "cy",   "cz",  "de",   "dj",   "dk", "dm", "do",
		"dz",   "ec",   "ee",   "eg",     "er",     "es",   "et",  "eu",   "fi",   "fj", "fk", "fm",
		"fo",   "fr",   "ga",   "gb",     "gd",     "ge",   "gf",  "gg",   "gh",   "gi", "gl", "gm",
		"gn",   "gp",   "gq",   "gr",     "gs",     "gt",   "gu",  "gw",   "gy",   "hk", "hm", "hn",
		"hr",   "ht",   "hu",   "id",     "ie",     "il",   "im",  "in",   "io",   "iq", "ir", "is",
		"it",   "je",   "jm",   "jo",     "jp",     "ke",   "kg",  "kh",   "ki",   "km", "kn", "kp",
		"kr",   "kw",   "ky",   "kz",     "la",     "lb",   "lc",  "li",   "lk",   "lr", "ls", "lt",
		"lu",   "lv",   "ly",   "ma",     "mc",     "md",   "me",  "mg",   "mh",   "mk", "ml", "mm",
		"mn",   "mo",   "mp",   "mq",     "mr",     "ms",   "mt",  "mu",   "mv",   "mw", "mx", "my",
		"mz",   "na",   "nc",   "ne",     "nf",     "ng",   "ni",  "nl",   "no",   "np", "nr", "nu",
		"nz",   "om",   "pa",   "pe",     "pf",     "pg",   "ph",  "pk",   "pl",   "pm", "pn", "pr",
		"ps",   "pt",   "pw",   "py",     "qa",     "re",   "ro",  "rs",   "ru",   "rw", "sa", "sb",
		"sc",   "sd",   "se",   "sg",     "sh",     "si",   "sj",  "sk",   "sl",   "sm", "sn", "so",
		"sr",   "st",   "su",   "sv",     "sy",     "sz",   "tc",  "td",   "tf",   "tg", "th", "tj",
		"tk",   "tl",   "tm",   "tn",     "to",     "tp",   "tr",  "tt",   "tv",   "tw", "tz", "ua",
		"ug",   "uk",   "us",   "uy",     "uz",     "va",   "vc",  "ve",   "vg",   "vi", "vn", "vu",
		"wf",   "ws",   "ye",   "yt",     "za",     "zm",   "zw",

		"aero", "arpa", "asia", "com",    "coop",   "info", "int", "jobs", "mobi",
		"gov",  "pro",  "tel",  "travel", "museum", "name", "biz", "cat",  "edu",
		"mil",  "net",  "org"
	};

	static private final String validUserinfoCharsData =
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789" +
		";:&=+$," +
		"-_.!~*()" +
		"%";

	static private final String validPathCharsData =
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"0123456789" +
		"/;?:@&=+$,%#" +
		"-_.!~*()" +
		"ŒŠš[]<>{}^|'`";     // Not so valid ones

	static private final String validSchemeCharsData =
		"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ" +
		"+-.";

	static private final Set<String>    countryCodes;
	static private final Set<Character> validUserinfoChars;
	static private final Set<Character> validPathChars;
	static private final Set<Character> validSchemeChars;

	private static Pattern hostRe;

	public static Filter   defaultFilter = Filter.NORMAL;

	static {
		countryCodes       = new HashSet<String>(Arrays.asList(countryCodeData));

		validUserinfoChars = new HashSet<Character>();
		validPathChars     = new HashSet<Character>();
		validSchemeChars   = new HashSet<Character>();

		for (char c : validUserinfoCharsData.toCharArray()) {
			validUserinfoChars.add(c);
		}

		for (char c : validPathCharsData.toCharArray()) {
			validPathChars.add(c);
		}

		for (char c : validSchemeCharsData.toCharArray()) {
			validSchemeChars.add(c);
		}

		String tmpHostname = String.format(hostnameCode, domainLabelCode, topLabelCode);
		String tmpIpv6     = String.format(ipv6Code, singleNumCode);
		String tmpHost     = String.format(hostCode, tmpHostname, ipv4Code, tmpIpv6);

		hostRe = Pattern.compile(tmpHost.replace(" ", ""), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
	}

	/**
	 * Finds every valid urls from string.
	 *
	 * @param input Any string maybe containing urls.
	 * @return List of urls.
	 */
	static ArrayList<String> findAll(String input) {

		return findAll(input, defaultFilter);
	}

	/**
	 * Finds every valid urls from string.
	 *
	 * @param input Any string maybe containing urls.
	 * @param filter For filtering valid urls.
	 * @return List of urls.
	 */
	static ArrayList<String> findAll(String input, Filter filter) {
		ArrayList<String> ret = new ArrayList<String>();

		Matcher m             = hostRe.matcher(input);

		int pos               = 0;

		while (m.find(pos)) {
			URL    url  = new URL();
			String ipv4 = m.group("ipv4");
			String ipv6 = m.group("ipv6");

			url.host    = m.group("host");
			url.hasPort = m.group("port") != null;

			if (m.group("hostname") != null) {

				url.type = Host.NAME;
			} else if (ipv4 != null) {

				url.type = Host.IPV4;
			} else if (ipv6 != null) {

				url.type = Host.IPV6;
			}

			switch (url.type) {
				case NAME:

					url.hasTLD = m.group("domainlabel") != null;

					if (url.hasTLD) {
						url.validTLD = countryCodes.contains(m.group("toplabel").toLowerCase());
					}

					break;

				case IPV4:

					url.validIP = true;

					for (String segment : ipv4.split("\\.")) {
						int val = parseInt(segment);

						if (val < 0 || val > 255) {
							url.validIP = false;
						}
					}

					break;

				case IPV6:
					url.validIP = true;
					break;

				default:
					break;
			}

			int start = m.start("host");
			int end   = m.end("host");

			if (start > 1 && input.charAt(start - 1) == '@') {
				start -= 1;

				while (start > 0 && validUserinfoChars.contains(input.charAt(start - 1))) {
					start--;

					url.hasUserinfo = true;
				}
			}

			if (start > 3 && input.substring(start - 3, start).equalsIgnoreCase("://")) {
				start -= 3;

				while (start > 0 && validSchemeChars.contains(input.charAt(start - 1))) {

					start--;

					url.hasScheme = true;
				}
			}

			if (end < input.length() && input.charAt(end) == '/') {
				end++;

				while (end < input.length() && validPathChars.contains(input.charAt(end))) {
					end++;
				}

				url.hasPath = true;
			}

			if (end < input.length() && input.charAt(end) != ' ') {
				pos = end;

				continue;
			}

			String urlData = input.substring(start, end);

			url.uri  = urlData;
			url.port = url.hasPort ? parseInt(m.group("port")) : 0;

			if (filter.validate(url)) {
				ret.add(urlData);
			}

			pos = end;
		}

		return ret;
	}

	public String  uri;
	public String  host;
	public int     port;
	public Host    type = URL.Host.NONE;
	public boolean hasTLD;
	public boolean hasPort;
	public boolean validTLD;
	public boolean validIP;
	public boolean hasScheme;
	public boolean hasPath;
	public boolean hasUserinfo;

	@Override
	public String toString() {
		return this.uri;
	}

	public interface Filter {
		boolean validate(URL url);

		public static final Filter STRICT = new Filter() {
			public boolean validate(URL url) {
				if (url.type == URL.Host.IPV4 || url.type == URL.Host.IPV6) {
					return url.validIP && url.hasScheme;
				}

				return url.validTLD && url.hasScheme;
			}
		};

		public static final Filter NORMAL = new Filter() {
			public boolean validate(URL url) {

				if (url.type == URL.Host.IPV4 || url.type == URL.Host.IPV6) {
					return url.validIP;
				}

				if (!url.hasScheme && !url.hasPort) {
					return url.validTLD;
				}

				return url.hasScheme || url.hasPort || url.hasTLD;
			}
		};

		public static final Filter SIMPLE = new Filter() {
			public boolean validate(URL url) {
				return url.validIP || url.hasScheme || url.hasPort || url.hasTLD;
			}
		};

		public static final Filter NONE = new Filter() {
			public boolean validate(URL url) {
				return true;
			}
		};

		public static final Filter WEB = new Filter() {
			public boolean validate(URL url) {
				if (url.hasScheme) {
					return url.uri.toLowerCase().startsWith("http") && url.validIP;
				}

				return NORMAL.validate(url);
			}
		};

		public static final Filter WEB_STRICT = new Filter() {
			public boolean validate(URL url) {

				return STRICT.validate(url) && url.uri.toLowerCase().startsWith("http");
			}
		};

		public static final Filter EMAIL = new Filter() {
			public boolean validate(URL url) {

				return !url.hasPath && url.hasUserinfo && (url.type == Host.NAME);
			}
		};

		public static final Filter EMAIL_STRICT = new Filter() {
			public boolean validate(URL url) {
				return EMAIL.validate(url) && url.validTLD;
			}
		};
	}

	// Just for developing and testing
	public static void main(String[] args) {

		URL.defaultFilter = URL.Filter.EMAIL;

		String input = "lorum ipsum kala foo: http://www.technikfoo.de/mai " +
					   "192.168.42.69/foobar  " +
					   "https://192.168.42.60/foobar  " +
					   "http://1234.123.123.123/foobar  " +
					   "1234.123.123.123/foobar  " +
					   "http://foo@bar.eu/~master  " +
					   "localhost:8080/jepajeee  " +
					   "kala.org  " +
					   "foo.bar  " +
					   "foo.fi  " +
					   "bar.SE  " +
					   "www.dotti.org  " +
					   "username+temp@domain.fi  " +
					   "mega.spammer@example.tld  " +
					   "megadomain/fooojbab  " +
					   "ssh-svn://user@host/foo/bar/baz  " +
					   "foo://kala.org  " +
					   "://kala/foo";

		for (String url : findAll(input)) {
			System.out.println("url = " + url);
		}
	}
}
