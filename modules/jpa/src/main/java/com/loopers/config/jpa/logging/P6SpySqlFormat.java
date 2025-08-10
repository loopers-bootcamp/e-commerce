package com.loopers.config.jpa.logging;

import com.p6spy.engine.logging.Category;
import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.FormatStyle;

import java.util.Locale;

public class P6SpySqlFormat implements MessageFormattingStrategy {

    private static final String NEW_LINE = "\n";
    private static final String TAP = "\t";
    private static final String CREATE = "create";
    private static final String ALTER = "alter";
    private static final String DROP = "drop";
    private static final String COMMENT = "comment";

    @Override
    public String formatMessage(
            int connectionId,
            String now,
            long elapsed,
            String category,
            String prepared,
            String sql,
            String url
    ) {
        if (sql.isBlank()) {
            return formatByCommand(category);
        }

        return formatBySql(sql, category) + getAdditionalMessages(elapsed);
    }

    private static String formatByCommand(String category) {
        return "%nExecute Command : %s%s%n----------------------------------------------------------------------------------------------------"
                .formatted(TAP, category).strip();
    }

    private String formatBySql(String sql, String category) {
        String type;
        if (isStatementDDL(sql, category)) {
            sql = FormatStyle.DDL.getFormatter().format(sql);
            type = "DDL";
        } else {
            sql = FormatStyle.BASIC.getFormatter().format(sql);
            type = "DML";
        }

        sql = FormatStyle.HIGHLIGHT.getFormatter().format(sql).strip();

        return "%nExecute %s :%n%n%s".formatted(type, sql);
    }

    private String getAdditionalMessages(long elapsed) {
        return "%n%n(Execution Time : %,d ms)%n----------------------------------------------------------------------------------------------------"
                .formatted(elapsed);
    }

    private boolean isStatementDDL(String sql, String category) {
        return isStatement(category) && isDDL(sql.strip().toLowerCase(Locale.ROOT));
    }

    private boolean isStatement(String category) {
        return Category.STATEMENT.getName().equals(category);
    }

    private boolean isDDL(String lowerSql) {
        return lowerSql.startsWith(CREATE)
                || lowerSql.startsWith(ALTER)
                || lowerSql.startsWith(DROP)
                || lowerSql.startsWith(COMMENT);
    }

}
