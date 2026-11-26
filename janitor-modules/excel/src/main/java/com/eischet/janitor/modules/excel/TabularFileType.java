// SPDX-FileCopyrightText: 2021-2026 Eischet Software e.K.
// SPDX-License-Identifier: MIT

package com.eischet.janitor.modules.excel;

public enum TabularFileType {

    EXCEL("excel"),
    CSV("csv"),
    CSV_UTF8("csv-utf8"),
    CSV_UTF16LE("csv-utf16le");

    private final String code;

    TabularFileType(final String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
