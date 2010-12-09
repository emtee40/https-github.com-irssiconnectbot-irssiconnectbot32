package org.irssibot.util;

import regex2.Matcher;
import regex2.Pattern;

import java.util.ArrayList;

/**
 * User: parkerkane
 * Date: 9.12.2010
 * Time: 14:50
 */
public class URL {

	// domainlabel   = alphanum | alphanum *( alphanum | "-" ) alphanum
	static private String domainLabelCode =
		"(?:" +
		"  [a-z0-9]" +
		"  |" +
		"  (?:" +
		"    [a-z0-9]" +
		"    [a-z0-9-]*" +
		"    [a-z0-9]" +
		"  )" +
		")";

	// toplabel      = alpha | alpha *( alphanum | "-" ) alphanum
	static private String topLabelCode =
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
	static private String hostnameCode =
		"(?<hostname>" +
		"  (?:%1$s[.])+" +                      // var: domainLabelCode
		"  %2$s" +                              // var: topLabelCode
		")";

	static private String ipv4Code =
		"(?<ipv4Code>" +
//		"  (?:(?<=[^0-9])|\\A)" +				// Doesn't start with number
		"  (?:[0-9]{1,3}\\.){3}[0-9]{1,3}" +    // '123.123.123.123'
		"  (?![0-9])" +                         // And doesn't end with number
		")";

	static private String singleNumCode = "[0-9a-f]{0,4}";

	static private String ipv6Code      =
		"(?:" +
		"  \\[" +                               // Start [
		"  (?<ipv6Code>" +
		"    (?:%1$s[:]){2,7}" +                // xxxx:xxxx:[xxxx:[xxxx:[xxxx:[xxxx:[xxxx:]]]]]
		"    %1$s" +                            // xxxx
		"  )" +
		"  \\]" +                               // End ]
		")";

	static private String hostCode =
		"(?<host>" +
		"  (?:" +                               // Must be one of these"+
		"    %1$s" +                            // var: hostnameCode"+
		"    |%2$s" +                           // var: ipv4Code"+
		"    |%3$s" +                           // var: ipv6Code"+
		"  )" +
		"  (?:" +                               // Check if port is defined"+
		"    :" +
		"     (?<port>" +
		"      [0-9]+" +
		"    )" +
		"  )?" +
		")";

	static private String[] countryCodes = {
		"ac",   "ad",  "ae",  "af",   "ag",  "ai",  "al",   "am",  "an",  "ao",
		"aq",   "ar",  "as",  "at",   "au",  "aw",  "ax",   "az",  "ba",  "bb",
		"bd",   "be",  "bf",  "bg",   "bh",  "bi",  "bj",   "bm",  "bn",  "bo",
		"br",   "bs",  "bt",  "bv",   "bw",  "by",  "bz",   "ca",  "cc",  "cd",
		"cf",   "cg",  "ch",  "ci",   "ck",  "cl",  "cm",   "cn",  "co",  "cr",
		"cs",   "cu",  "cv",  "cx",   "cy",  "cz",  "de",   "dj",  "dk",  "dm",
		"do",   "dz",  "ec",  "ee",   "eg",  "eh",  "er",   "es",  "et",  "fi",
		"fj",   "fk",  "fm",  "fo",   "fr",  "ga",  "gb",   "gd",  "ge",  "gf",
		"gg",   "gh",  "gi",  "gl",   "gm",  "gn",  "gp",   "gq",  "gr",  "gs",
		"gt",   "gu",  "gw",  "gy",   "hk",  "hm",  "hn",   "hr",  "ht",  "hu",
		"id",   "ie",  "il",  "im",   "in",  "io",  "iq",   "ir",  "is",  "it",
		"je",   "jm",  "jo",  "jp",   "ke",  "kg",  "kh",   "ki",  "km",  "kn",
		"kp",   "kr",  "kw",  "ky",   "kz",  "la",  "lb",   "lc",  "li",  "lk",
		"lr",   "ls",  "lt",  "lu",   "lv",  "ly",  "ma",   "mc",  "md",  "mg",
		"mh",   "mk",  "ml",  "mm",   "mn",  "mo",  "mp",   "mq",  "mr",  "ms",
		"mt",   "mu",  "mv",  "mw",   "mx",  "my",  "mz",   "na",  "nc",  "ne",
		"nf",   "ng",  "ni",  "nl",   "no",  "np",  "nr",   "nu",  "nz",  "om",
		"pa",   "pe",  "pf",  "pg",   "ph",  "pk",  "pl",   "pm",  "pn",  "pr",
		"ps",   "pt",  "pw",  "py",   "qa",  "re",  "ro",   "ru",  "rw",  "sa",
		"sb",   "sc",  "sd",  "se",   "sg",  "sh",  "si",   "sj",  "sk",  "sl",
		"sm",   "sn",  "so",  "sr",   "st",  "sv",  "sy",   "sz",  "tc",  "td",
		"tf",   "tg",  "th",  "tj",   "tk",  "tl",  "tm",   "tn",  "to",  "tp",
		"tr",   "tt",  "tv",  "tw",   "tz",  "ua",  "ug",   "uk",  "um",  "us",
		"uy",   "uz",  "va",  "vc",   "ve",  "vg",  "vi",   "vn",  "vu",  "wf",
		"ws",   "ye",  "yt",  "yu",   "za",  "zm",  "zw",

		// Special TLDs
		"aero", "biz", "com", "coop", "edu", "gov", "info", "int", "mil", "museum",
		"name", "net", "org", "pro",  "me"
	};

	private static Pattern host = Pattern.compile(buildRegexCode(), Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	private static String buildRegexCode() {

		String tmpHostname = String.format(hostnameCode, domainLabelCode, topLabelCode);
		String tmpIpv6     = String.format(ipv6Code, singleNumCode);
		String tmpHost     = String.format(hostCode, tmpHostname, ipv4Code, tmpIpv6);

		return tmpHost.replace(" ", "");
	}

	/**
	 * Finds every valid urls from string.
	 *
	 * @param string Any string maybe containing urls.
	 * @return List of urls.
	 */
	static ArrayList<String> findAll(String string) {
		return findAll(string, true);
	}

	/**
	 * Finds every valid urls from string.
	 *
	 * @param string Any string maybe containing urls.
	 * @param needScheme Is scheme required or not
	 * @return List of urls.
	 */
	static ArrayList<String> findAll(String string, Boolean needScheme) {
		ArrayList<String> ret = new ArrayList<String>();

		// TODO: Everything! Profit!

		return ret;
	}

	public static void main(String[] args) {

		Matcher m = host.matcher("http://haukionkala.foo/asfasdfsa  asdf  http://jeejeee.org/foobar.bz");

		int pos=0;
		
		while (m.find(pos)) {
			System.out.println(m.group("host"));
			System.out.println(m.group("toplabel"));

			System.out.println(m.start("host"));
			System.out.println(m.end("host"));
			
			pos = m.end();
		}
	}
}
