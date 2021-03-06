package com.electronwill.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for manipulating Strings. It provides faster methods than the standard Java
 * library.
 *
 * @author TheElectronWill
 */
public final class StringUtils {
	/**
	 * Removes every occurence of a character from a String.
	 *
	 * @return a new String contaning the given String without the given character
	 */
	public static String remove(String seq, char toRemove) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.length(); i++) {
			final char ch = seq.charAt(i);
			if (ch != toRemove) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Removes several character from a String.
	 *
	 * @return a new String contaning the given String without the given characters
	 */
	public static String remove(String seq, char... toRemove) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < seq.length(); i++) {
			final char ch = seq.charAt(i);
			boolean append = true;
			for (final char c : toRemove) {
				if (ch == c) {
					append = false;
					break;
				}
			}
			if (append) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}

	/**
	 * Splits a String around each occurence of the specified character. The result is <b>not</b>
	 * the same as {@link String#split(String)}. In particular, this method never returns an
	 * empty list.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code split("a.b.c", '.')} gives {@code ["a", "b", "c"]}
	 * <li>{@code split("", '.')} gives {@code [""]} (a list containing the empty string)
	 * <li>{@code split(".", '.')} gives {@code ["", ""]} (a list containing two empty strings)
	 * <li>{@code split("..", '.')} gives {@code ["", "", ""]} (a list containing three empty
	 * strings)
	 * <li>{@code split(".a...b.", '.')} gives {@code ["", "a", "", "", "b", ""]} (a list containing
	 * an empty string, the string "a", two empty strings, the string "b", and an empty string)
	 * </ul>
	 *
	 * @param str the String to split
	 * @param sep the separator to use
	 * @return a non-empty list of strings
	 */
	public static List<String> split(String str, char sep) {
		List<String> list = new ArrayList<>(4);
		split(str, sep, list);
		return list;
	}

	/**
	 * Splits a String around each occurence of the specified character, and puts the result in the
	 * given List. The result is <b>not</b> the same as {@link String#split(String)}. In
	 * particular, this method always add at least one element to the list.
	 * <p>
	 * Examples:
	 * <ul>
	 * <li>{@code split("a.b.c", '.')} gives {@code ["a", "b", "c"]}
	 * <li>{@code split("", '.')} gives {@code [""]} (a list containing the empty string)
	 * <li>{@code split(".", '.')} gives {@code ["", ""]} (a list containing two empty strings)
	 * <li>{@code split("..", '.')} gives {@code ["", "", ""]} (a list containing three empty
	 * strings)
	 * <li>{@code split(".a...b.", '.')} gives {@code ["", "a", "", "", "b", ""]} (a list containing
	 * an empty string, the string "a", two empty strings, the string "b", and an empty string)
	 * </ul>
	 *
	 * @param str  the String to split
	 * @param sep  the separator to use
	 * @param list the list where to put the results
	 */
	public static void split(String str, char sep, List<String> list) {
		int pos0 = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == sep) {// separator found
				list.add(str.substring(pos0, i));
				pos0 = i + 1;
			}
		}
		list.add(str.substring(pos0, str.length()));// adds the last part
	}

	/**
	 * Splits a command String by isolating each argument. An argument is a sequence of chars
	 * surrounded by quotes (double or simple) or by spaces (if not in quotes).
	 *
	 * @param str the String to split
	 * @return a non-empty list of Strings
	 */
	public static List<String> splitArguments(String str) {
		List<String> list = new ArrayList<>();
		splitArguments(str, list);
		return list;
	}

	/**
	 * Splits a command String by isolating each argument. An argument is a sequence of chars
	 * surrounded by quotes (double or simple) or by spaces (if not in quotes).
	 *
	 * @param str the String to split
	 * @param list the list where to put the results
	 */
	public static void splitArguments(String str, List<String> list) {
		StringBuilder sb = new StringBuilder();
		int pos = 0;
		boolean escape = false, inQuotes = false;
		while (pos < str.length()) {
			char ch = str.charAt(pos++);
			if (escape) {
				escape = false;
				sb.append(ch);
			} else if (ch == '\\') {
				escape = true;
			} else if (ch == '"' || ch == '\'') {
				if (inQuotes) {
					inQuotes = false;
					list.add(sb.toString());
					sb.setLength(0);
				} else {
					inQuotes = true;
				}
			} else if (ch == ' ' && !inQuotes) {
				if (sb.length() > 0) {
					list.add(sb.toString());
					sb.setLength(0);
				}
			} else {
				sb.append(ch);
			}
		}
		if (sb.length() > 0) {
			list.add(sb.toString());
		}
	}

  /**
   * Checks if the given String is made exclusively of the given chars.
   *
   * @param str the String to check
   * @param chars the allowed characters
   * @return true if the string only contains allowed characters, false if it contains some others
   */
	public static boolean containsOnly(String str, char... chars) {
	  boolean allowed = true;
		for (int i = 0; allowed && i < str.length(); i++) {
			final char ch = str.charAt(i);
			boolean isOk = false;
      for (int i1 = 0; !isOk && i1 < chars.length; i1++) {
        char c = chars[i1];
        if (ch == c) {
          isOk = true;
        }
      }
      allowed = isOk;
		}
		return allowed;
	}

  /**
   * Checks if the given String contains any of the given chars.
   *
   * @param str the String to check
   * @param chars the characters to search
   * @return true if the string contains at least one occurence of at least one of the given chars
   */
  public static boolean containsAny(String str, char... chars) {
    for (int i = 0; i < str.length(); i++) {
      final char ch = str.charAt(i);
      for (char c : chars) {
        if (ch == c) {
          return true;
        }
      }
    }
    return false;
  }

	private StringUtils() {}
}
