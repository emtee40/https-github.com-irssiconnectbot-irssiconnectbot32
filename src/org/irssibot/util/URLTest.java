package org.irssibot.util;

import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * User: treunanen
 * Date: 10.12.2010
 * Time: 16:05
 */
public class URLTest
	extends TestCase {

	public static final String INPUT = "lorum ipsum kala foo: http://www.technikfoo.de/mai " +
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

	public void setUp()
		throws Exception {

	}

	public void tearDown()
		throws Exception {

	}

	private static void compareFindAll(String msg, URL.Filter filter, Object... excepted) {

		ArrayList<String> actual = URL.findAll(INPUT, filter);

		StringBuffer sb = new StringBuffer();

		for (String s : actual) {
			sb.append('"');
			sb.append(s);
			sb.append("\", ");
		}

		System.out.println("sb.toString() = " + sb.toString());

		assertEquals(msg, excepted.length, actual.size());

		for (int i = 0; i < excepted.length; i++) {
			assertEquals(msg, excepted[i].toString(), actual.get(i));
		}
	}

	public void testFindAllEmailStrict()
		throws Exception {

		ArrayList<String> arr = URL.findAll(INPUT, URL.Filter.EMAIL_STRICT);

		String msg = "Email Strict";

		assertEquals(msg, arr.size(), 1);
		assertEquals(msg, arr.get(0), "username+temp@domain.fi");
	}

	public void testFindAllEmail() {

		compareFindAll(
			"Email",
			URL.Filter.EMAIL,
			"username+temp@domain.fi",
			"mega.spammer@example.tld");
	}

	public void testFindAllWebStrict() {

		compareFindAll(
			"Web Strict",
			URL.Filter.WEB_STRICT,
			"http://www.technikfoo.de/mai",
			"https://192.168.42.60/foobar",
			"http://foo@bar.eu/~master");
	}

	public void testFindAllWeb() {

		compareFindAll(
			"Web",
			URL.Filter.WEB,
			"192.168.42.69/foobar",
			"https://192.168.42.60/foobar",
			"localhost:8080/jepajeee",
			"kala.org",
			"foo.fi",
			"bar.SE",
			"www.dotti.org",
			"username+temp@domain.fi"

		);
	}

	public void testFindAllNone() {

		compareFindAll(
			"None",

			URL.Filter.NONE,

			"lorum",
			"ipsum",
			"kala",
			"http://www.technikfoo.de/mai",
			"192.168.42.69/foobar",
			"https://192.168.42.60/foobar",
			"http://1234.123.123.123/foobar",
			"1234.123.123.123/foobar",
			"http://foo@bar.eu/~master",
			"localhost:8080/jepajeee",
			"kala.org",
			"foo.bar",
			"foo.fi",
			"bar.SE",
			"www.dotti.org",
			"username+temp@domain.fi",
			"mega.spammer@example.tld",
			"megadomain/fooojbab",
			"ssh-svn://user@host/foo/bar/baz",
			"foo://kala.org",
			"://kala/foo"
		);
	}

	public void testAllSimple() {

		compareFindAll(
			"Simple",
			URL.Filter.SIMPLE,
			"http://www.technikfoo.de/mai",
			"192.168.42.69/foobar",
			"https://192.168.42.60/foobar",
			"http://1234.123.123.123/foobar",
			"http://foo@bar.eu/~master",
			"localhost:8080/jepajeee",
			"kala.org",
			"foo.bar",
			"foo.fi",
			"bar.SE",
			"www.dotti.org",
			"username+temp@domain.fi",
			"mega.spammer@example.tld",
			"ssh-svn://user@host/foo/bar/baz",
			"foo://kala.org"
		);
	}

	public void testFindAllNormal() {

		compareFindAll(
			"Simple",
			URL.Filter.NORMAL,

			"http://www.technikfoo.de/mai",
			"192.168.42.69/foobar",
			"https://192.168.42.60/foobar",
			"http://foo@bar.eu/~master",
			"localhost:8080/jepajeee",
			"kala.org",
			"foo.fi",
			"bar.SE",
			"www.dotti.org",
			"username+temp@domain.fi",
			"ssh-svn://user@host/foo/bar/baz",
			"foo://kala.org"
		);
	}

	public void testFindAllStrict() {

		compareFindAll(
			"Strict",
			URL.Filter.STRICT,
			"http://www.technikfoo.de/mai",
			"https://192.168.42.60/foobar",
			"http://foo@bar.eu/~master",
			"foo://kala.org"
		);
	}
}
