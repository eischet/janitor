/*
 NaturalOrderComparator.java -- Perform 'natural order' comparisons of strings in Java.
 Copyright (C) 2003 by Pierre-Luc Paour <natorder@paour.com>

 Based on the C version by Martin Pool, of which this is more or less a straight conversion.
 Copyright (C) 2000 by Martin Pool <mbp@humbug.org.au>

 This software is provided 'as-is', without any express or implied
 warranty.  In no event will the authors be held liable for any damages
 arising from the use of this software.

 Permission is granted to anyone to use this software for any purpose,
 including commercial applications, and to alter it and redistribute it
 freely, subject to the following restrictions:

 1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
 2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
 3. This notice may not be removed or altered from any source distribution.


 ORIGINAL: https://github.com/paour/natorder
 */


package com.eischet.janitor.toolbox.strings;


import java.util.Comparator;

public class NaturalOrderComparator implements Comparator<String> {
    static int compareRight(String a, String b) {
        int bias = 0, ia = 0, ib = 0;

        // The longest run of digits wins. That aside, the greatest
        // value wins, but we can't know that it will until we've scanned
        // both numbers to know that they have the same magnitude, so we
        // remember it in BIAS.
        for (; ; ia++, ib++) {
            char ca = charAt(a, ia);
            char cb = charAt(b, ib);

            if (!isDigit(ca) && !isDigit(cb)) {
                return bias;
            }
            if (!isDigit(ca)) {
                return -1;
            }
            if (!isDigit(cb)) {
                return +1;
            }
            if (ca == 0 && cb == 0) {
                return bias;
            }

            if (bias == 0) {
                if (ca < cb) {
                    bias = -1;
                } else if (ca > cb) {
                    bias = 1;
                }
            }
        }
    }


    public static int doCompare(String o1, String o2) {
        int ia = 0, ib = 0;
        int nza = 0, nzb = 0;
        char ca, cb;

        while (true) {
            // Only count the number of zeroes leading the last number compared
            nza = nzb = 0;

            ca = charAt(o1, ia);
            cb = charAt(o2, ib);

            // skip over leading spaces or zeros
            while (Character.isSpaceChar(ca) || ca == '0') {
                if (ca == '0') {
                    nza++;
                } else {
                    // Only count consecutive zeroes
                    nza = 0;
                }

                ca = charAt(o1, ++ia);
            }

            while (Character.isSpaceChar(cb) || cb == '0') {
                if (cb == '0') {
                    nzb++;
                } else {
                    // Only count consecutive zeroes
                    nzb = 0;
                }

                cb = charAt(o2, ++ib);
            }

            // Process run of digits
            if (Character.isDigit(ca) && Character.isDigit(cb)) {
                int bias = compareRight(o1.substring(ia), o2.substring(ib));
                if (bias != 0) {
                    return bias;
                }
            }

            if (ca == 0 && cb == 0) {
                // The strings compare the same. Perhaps the caller
                // will want to call strcmp to break the tie.
                return compareEqual(o1, o2, nza, nzb);
            }
            if (ca < cb) {
                return -1;
            }
            if (ca > cb) {
                return +1;
            }

            ++ia;
            ++ib;
        }
    }

    @Override
    public int compare(String o1, String o2) {
        return doCompare(o1, o2);
    }

    static boolean isDigit(char c) {
        return Character.isDigit(c) || c == '.' || c == ',';
    }

    static char charAt(String s, int i) {

        return i >= s.length() ? 0 : Character.toLowerCase(s.charAt(i)); // should provide case-independent comparison
        // origiunal: return i >= s.length() ? 0 : s.charAt(i);
    }

    static int compareEqual(String a, String b, int nza, int nzb) {
        if (nza - nzb != 0) {
            return nza - nzb;
        }
        if (a.length() == b.length()) {
            return a.compareTo(b);
        }
        return a.length() - b.length();
    }


}