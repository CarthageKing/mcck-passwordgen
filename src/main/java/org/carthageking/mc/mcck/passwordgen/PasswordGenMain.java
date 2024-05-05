package org.carthageking.mc.mcck.passwordgen;

/*-
 * #%L
 * mcck-passwordgen
 * %%
 * Copyright (C) 2024 Michael I. Calderero
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.PrintWriter;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class PasswordGenMain {

	private static final String OPT_CHARS = "chars";
	private static final String OPT_LENGTH = "length";
	private static final String OPT_MIN_UPPER = "minUpper";
	private static final String OPT_MIN_LOWER = "minLower";
	private static final String OPT_MIN_DIGIT = "minDigit";
	private static final String OPT_MIN_SPECIAL = "minSpecial";
	private static final String OPT_MAX_REPEAT = "maxRepeat";

	private static final int LOWEST_LENGTH = 6;
	private static final int DEFAULT_LENGTH = 16;
	private static final int DEFAULT_MIN_CHAR = 3;
	private static final int DEFAULT_MAX_REPEAT = 1;

	private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
	private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	private static final String DIGIT = "0123456789";
	private static final String SPECIAL = "`~!@#$%^&*()-_=+\\ |]}[{'\";:/?.>,<";
	private static final String DEFAULT_CHARS = UPPER + DIGIT + LOWER + SPECIAL;

	public static void main(String[] args) throws Exception {
		Options opts = creatOptions();

		try {
			CommandLine cl = new DefaultParser().parse(opts, args);
			doMain(cl);
			System.exit(0);
		} catch (ParseException e) {
			System.err.println(e.getMessage());
			displayUsage();
			System.exit(1);
		}
	}

	private static Options creatOptions() {
		String[] args = {
			OPT_CHARS,
			OPT_LENGTH,
			OPT_MIN_UPPER,
			OPT_MIN_LOWER,
			OPT_MIN_DIGIT,
			OPT_MIN_SPECIAL,
			OPT_MAX_REPEAT,
		};
		String[] desc = {
			"Chars to use. A - uppercase letter, a - lowercase letter, D - digit, S - special. e.g. use 'AaDS' to use everything, or use 'AD' to generate using only uppercase letters and digits. By default, will use 'AaDS'",
			"Generated password length. Minimum length is " + LOWEST_LENGTH + ". Default length is " + DEFAULT_LENGTH,
			"Minimum number of uppercase letters. Default is " + DEFAULT_MIN_CHAR,
			"Minimum number of lowercase letters. Default is " + DEFAULT_MIN_CHAR,
			"Minimum number of digits. Default is " + DEFAULT_MIN_CHAR,
			"Minimum number of special characters. Default is " + DEFAULT_MIN_CHAR,
			"Any single char can repeat at most this number. Default is " + DEFAULT_MAX_REPEAT,
		};
		Options opts = new Options();
		for (int i = 0; i < args.length; i++) {
			Option opt = new Option(args[i], null, true, desc[i]);
			opt.setRequired(false);
			opts.addOption(opt);
		}
		return opts;
	}

	private static void displayUsage() {
		HelpFormatter fmt = new HelpFormatter();
		PrintWriter pw = new PrintWriter(System.err);
		fmt.printHelp(pw, fmt.getWidth(), "thecmd", null, creatOptions(), fmt.getLeftPadding(), fmt.getDescPadding(), null);
		pw.flush();
	}

	private static void doMain(CommandLine cl) throws ParseException {
		String chars = StringUtils.trimToEmpty(cl.getOptionValue(OPT_CHARS, "AaDS"));
		String actualCharsToUse = "";
		if (chars.contains("A")) {
			actualCharsToUse += UPPER;
		}
		if (chars.contains("a")) {
			actualCharsToUse += LOWER;
		}
		if (chars.contains("D")) {
			actualCharsToUse += DIGIT;
		}
		if (chars.contains("S")) {
			actualCharsToUse += SPECIAL;
		}
		if (actualCharsToUse.isEmpty()) {
			actualCharsToUse = DEFAULT_CHARS;
		}

		int length = Integer.parseInt(StringUtils.trimToEmpty(cl.getOptionValue(OPT_LENGTH, String.valueOf(DEFAULT_LENGTH))));
		if (length < LOWEST_LENGTH) {
			throw new ParseException("Min length must be greater than or equal to " + LOWEST_LENGTH);
		}

		int minUpper = Integer.parseInt(StringUtils.trimToEmpty(cl.getOptionValue(OPT_MIN_UPPER, String.valueOf(DEFAULT_MIN_CHAR))));
		int minLower = Integer.parseInt(StringUtils.trimToEmpty(cl.getOptionValue(OPT_MIN_LOWER, String.valueOf(DEFAULT_MIN_CHAR))));
		int minDigit = Integer.parseInt(StringUtils.trimToEmpty(cl.getOptionValue(OPT_MIN_DIGIT, String.valueOf(DEFAULT_MIN_CHAR))));
		int minSpecial = Integer.parseInt(StringUtils.trimToEmpty(cl.getOptionValue(OPT_MIN_SPECIAL, String.valueOf(DEFAULT_MIN_CHAR))));
		int maxRepeat = Integer.parseInt(StringUtils.trimToEmpty(cl.getOptionValue(OPT_MAX_REPEAT, String.valueOf(DEFAULT_MAX_REPEAT))));

		Random rand = new Random();
		String generated = null;
		int tries = 0;
		int maxTries = 10_000;
		do {
			if (tries >= maxTries) {
				throw new RuntimeException("Failed to generate password matching criteria after " + maxTries + " tries");
			}
			int[] repeats = new int[actualCharsToUse.length()];
			tries++;
			char[] arr = new char[length];
			for (int i = 0; i < length; i++) {
				int idx = rand.nextInt(actualCharsToUse.length());

				arr[i] = actualCharsToUse.charAt(idx);
				repeats[idx]++;
				if (repeats[idx] > maxRepeat) {
					i--;
					tries++;
					if (tries >= maxTries) {
						throw new RuntimeException("Failed to generate password matching criteria after " + maxTries + " tries");
					}
				}
			}
			generated = StringUtils.trimToEmpty(new String(arr));
		} while (!(length == generated.length()
			&& hasMinRequired(UPPER, generated, minUpper)
			&& hasMinRequired(LOWER, generated, minLower)
			&& hasMinRequired(DIGIT, generated, minDigit)
			&& hasMinRequired(SPECIAL, generated, minSpecial)
			&& hasMaxRepeat(maxRepeat, generated)));

		System.out.println(generated);
	}

	private static boolean hasMaxRepeat(int maxRepeat, String generated) {
		for (int i = 0; i < generated.length(); i++) {
			char rc = generated.charAt(i);
			int repeat = 0;
			for (int j = 0; j < generated.length(); j++) {
				if (rc == generated.charAt(j)) {
					repeat++;
				}
			}
			if (repeat > maxRepeat) {
				return false;
			}
		}
		return true;
	}

	private static boolean hasMinRequired(String charstr, String genstr, int minRequired) {
		int count = 0;
		for (int i = 0; i < genstr.length(); i++) {
			char c = genstr.charAt(i);
			if (charstr.indexOf(c) >= 0) {
				count++;
			}
		}
		return count >= minRequired;
	}
}
