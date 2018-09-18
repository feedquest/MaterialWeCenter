package com.langtaosha.sjwyd;


import org.apache.commons.lang3.StringEscapeUtils;

public class Util {

    /**
     * 将HTML转换为BBCode
     * @param html: HTML字符串
     * @return BBCode字符串
     */
    public static String htmlToBBcode(String html) {
        html = html.replace("<br>", "\n");
        html = html.replace("<i>", "[i]");
        html = html.replace("</i>", "[/i]");
        html = html.replace("<b>", "[b]");
        html = html.replace("</b>", "[/b]");
        html = html.replace("<u>", "[u]");
        html = html.replace("</u>", "[/u]");
        html = html.replace("<blockquote>", "[quote]");
        html = html.replace("</blockquote>", "[/quote]");
        html = html.replace("<ul>", "[list]");
        html = html.replace("</ul>", "[/list]");
        html = html.replace("<li>", "[*]");
        html = html.replace("</li>", "[/*]");
        html = html.replaceAll("<a\\shref=\"(.*)\">", "[url=$1]");
        html = html.replace("</a>", "[/url]");
        html = StringEscapeUtils.unescapeHtml4(html);
        return html;
    }

}
