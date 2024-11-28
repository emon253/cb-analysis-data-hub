package org.carbarn.scrapify;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModelCodeExtractor {

    public static void main(String[] args) {
        String ocrText = "プライムRコーナー\n" +
                "NO\n" +
                "車歴(自家用以外は記入) 排気量\n" +
                "型式\n" +
                "評価点\n" +
                "1500cc\n" +
                "DAA-GP7\n" +
                "29211\n" +
                "初度登録年月 車名\n" +
                "形状・ドア数 グレード\n" +
                "2WD\n" +
                "H28\n" +
                "5 HYBRIDX\n" +
                "4\n" +
                "3月\n" +
                "シャトル\n" +
                "STYLE EDITION 4WD\n" +
                "内装\n" +
                "補助評価 C\n" +
                "車検\n" +
                "R7年 3月 シフト\n" +
                "A/T\n" +
                "SR 純AW\n" +
                "PWPS\n" +
                "走行///\n" +
                "089\n" +
                "Km\n" +
                "カワ TV\n" +
                "ナビ エアB\n" +
                "外色燃料輸入車用\n" +
                "外元色\n" +
                "色替\n" +
                "パールー\n" +
                "ガソリン・軽油・(\n" +
                "年式\n" +
                "リサイクル\n" +
                "預託金\n" +
                "9600円\n" +
                ")\n" +
                "マイル:\n" +
                "カラーN\n" +
                "輸入区分 ハンドル\n" +
                "ディーラー・並行 左・右\n" +
                "乗車定員\n" +
                "◎注意事項(修復・不具合箇所および状態等)\n" +
                "冷房\n" +
                "A/C 「セールスポイント\n" +
                "NH7889\n" +
                "新車整備手帳有・無\n" +
                "内装色\n" +
                "※書類と一緒に保管下さい。\n" +
                "「名義変更期限\n" +
                "スマートキー・LEDヘッドライト\n" +
                "フォグランプ・ネェストメモリーナビ\n" +
                "バックカメラ、ETC.\n" +
                "月\n" +
                "積載量\n" +
                "5人\n" +
                "t\n" +
                "クルーズコントロール日\n" +
                "登録No 川越 530±6214\n" +
                "車台No\n" +
                "シリアル No.\n" +
                "997-10349472\n" +
                "AA初出品\n" +
                "スタットレスタイヤ、\n" +
                "◎検査員報告(USS使用欄)\n" +
                "ハーム内汚レスレ 動物\n" +
                "なキズは\n" +
                "【荷台内寸】約\n" +
                "X\n" +
                "X\n" +
                "(cm)\n" +
                "長さ 440 cm 幅 169 Cm 高さ154 cm\n" +
                "Aul\n" +
                "スリムズレ\n" +
                "Al\n" +
                "LHA\n" +
                "(車検証上の寸法)\n" +
                "12\n" +
                "スペア";

        // Debugging: Print the OCR text to verify its contents
        System.out.println("OCR Text:");
        System.out.println(ocrText);

        // Normalize the text by removing unnecessary whitespace
        ocrText = ocrText.replaceAll("\\s+", " ");

        // Debugging: Print the normalized text
        System.out.println("Normalized OCR Text:");
        System.out.println(ocrText);

        // Define the regular expression pattern to find the model code
        String patternString = "型式\\s*([A-Z0-9-]+)";
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(ocrText);

        // Debugging: Check if the pattern matches the text
        if (matcher.find()) {
            String modelCode = matcher.group(1);
            System.out.println("Model Code: " + modelCode);
        } else {
            System.out.println("Model code not found.");
        }
    }
}
