(function($) {
    /**
     * Japanese language package
     * Translated by @tsuyoshifujii
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': '有効なBase64エンコードを入力してください'
        },
        between: {
            'default': '%sから%sの間で入力してください',
            notInclusive: '厳密に%sから%sの間で入力してください'
        },
        callback: {
            'default': '有効な値を入力してください'
        },
        choice: {
            'default': '有効な値を入力してください',
            less: '最低でも%sを選択してください',
            more: '最大でも%sを選択してください',
            between: '%s - %s で選択してください'
        },
        color: {
            'default': '有効なカラーコードを入力してください'
        },
        creditCard: {
            'default': '有効なクレジットカード番号を入力してください'
        },
        cusip: {
            'default': '有効なCUSIP番号を入力してください'
        },
        cvv: {
            'default': '有効なCVV番号を入力してください'
        },
        date: {
            'default': '有効な日付を入力してください',
            min: '%s 後に有効な日付を入力してください',
            max: '%s の前に有効な日付を入力してください',
            range: '%s - %s の間に有効な日付を入力してください'
        },
        different: {
            'default': '異なる値を入力してください'
        },
        digits: {
             'default': '数字のみで入力してください'
        },
        ean: {
            'default': '有効なEANコードを入力してください'
        },
        emailAddress: {
            'default': '有効なメールアドレスを入力してください'
        },
        file: {
            'default': '有効なファイルを選択してください'
        },
        greaterThan: {
            'default': '%sより大きい値を入力してください',
            notInclusive: '%sより大きい値を入力してください'
        },
        grid: {
            'default': '有効なGRIdコードを入力してください'
        },
        hex: {
            'default': '有効な16進数を入力してください。'
        },
        hexColor: {
            'default': '有効なカラーコード(RGB 16進数)を入力してください'
        },
        iban: {
            'default': '有効なIBANコードを入力してください',
            countryNotSupported: 'カントリーコード%sはサポートされていません',
            country: '有効な%sのIBANコードを入力してください',
            countries: {
                AD: 'アンドラ',
                AE: 'アラブ首長国連邦',
                AL: 'アルバニア',
                AO: 'アンゴラ',
                AT: 'オーストリア',
                AZ: 'アゼルバイジャン',
                BA: 'ボスニア·ヘルツェゴビナ',
                BE: 'ベルギー',
                BF: 'ブルキナファソ',
                BG: 'ブルガリア',
                BH: 'バーレーン',
                BI: 'ブルンジ',
                BJ: 'ベナン',
                BR: 'ブラジル',
                CH: 'スイス',
                CI: '象牙海岸',
                CM: 'カメルーン',
                CR: 'コスタリカ',
                CV: 'カーボベルデ',
                CY: 'キプロス',
                CZ: 'チェコ共和国',
                DE: 'ドイツ',
                DK: 'デンマーク',
                DO: 'ドミニカ共和国',
                DZ: 'アルジェリア',
                EE: 'エストニア',
                ES: 'スペイン',
                FI: 'フィンランド',
                FO: 'フェロー諸島',
                FR: 'フランス',
                GB: 'イギリス',
                GE: 'グルジア',
                GI: 'ジブラルタル',
                GL: 'グリーンランド',
                GR: 'ギリシャ',
                GT: 'グアテマラ',
                HR: 'クロアチア',
                HU: 'ハンガリー',
                IE: 'アイルランド',
                IL: 'イスラエル',
                IR: 'イラン',
                IS: 'アイスランド',
                IT: 'イタリア',
                JO: 'ヨルダン',
                KW: 'クウェート',
                KZ: 'カザフスタン',
                LB: 'レバノン',
                LI: 'リヒテンシュタイン',
                LT: 'リトアニア',
                LU: 'ルクセンブルグ',
                LV: 'ラトビア',
                MC: 'モナコ',
                MD: 'モルドバ',
                ME: 'モンテネグロ',
                MG: 'マダガスカル',
                MK: 'マケドニア',
                ML: 'マリ',
                MR: 'モーリタニア',
                MT: 'マルタ',
                MU: 'モーリシャス',
                MZ: 'モザンビーク',
                NL: 'オランダ',
                NO: 'ノルウェー',
                PK: 'パキスタン',
                PL: 'ポーランド',
                PS: 'パレスチナ',
                PT: 'ポルトガル',
                QA: 'カタール',
                RO: 'ルーマニア',
                RS: 'セルビア',
                SA: 'サウジアラビア',
                SE: 'スウェーデン',
                SI: 'スロベニア',
                SK: 'スロバキア',
                SM: 'サン·マリノ',
                SN: 'セネガル',
                TN: 'チュニジア',
                TR: 'トルコ',
                VG: '英領バージン諸島'
            }
        },
        id: {
            'default': '有効なIDを入力してください',
            countryNotSupported: 'カントリーコード%sはサポートされていません',
            country: '有効な%sのIDを入力してください',
            countries: {
                BA: 'スニア·ヘルツェゴビナ',
                BG: 'ブルガリア',
                BR: 'ブラジル',
                CH: 'スイス',
                CL: 'チリ',
                CN: 'チャイナ',
                CZ: 'チェコ共和国',
                DK: 'デンマーク',
                EE: 'エストニア',
                ES: 'スペイン',
                FI: 'フィンランド',
                HR: 'クロアチア',
                IE: 'アイルランド',
                IS: 'アイスランド',
                LT: 'リトアニア',
                LV: 'ラトビア',
                ME: 'モンテネグロ',
                MK: 'マケドニア',
                NL: 'オランダ',
                RO: 'ルーマニア',
                RS: 'セルビア',
                SE: 'スウェーデン',
                SI: 'スロベニア',
                SK: 'スロバキア',
                SM: 'サン·マリノ',
                TH: 'タイ国',
                ZA: '南アフリカ'
            }
        },
        identical: {
            'default': '同じ値を入力してください'
        },
        imei: {
            'default': '有効なIMEIを入力してください'
        },
        imo: {
            'default': '有効なIMOを入力してください'
        },
        integer: {
            'default': '有効な数値を入力してください'
        },
        ip: {
            'default': '有効なIPアドレスを入力してください',
            ipv4: '有効なIPv4アドレスを入力してください',
            ipv6: '有効なIPv6アドレスを入力してください'
        },
        isbn: {
            'default': '有効なISBN番号を入力してください'
        },
        isin: {
            'default': '有効なISIN番号を入力してください'
        },
        ismn: {
            'default': '有効なISMN番号を入力してください'
        },
        issn: {
            'default': '有効なISSN番号を入力してください'
        },
        lessThan: {
            'default': '%s未満の値を入力してください',
            notInclusive: '%s未満の値を入力してください'
        },
        mac: {
            'default': '有効なMACアドレスを入力してください'
        },
        meid: {
            'default': '有効なMEID番号を入力してください'
        },
        notEmpty: {
            'default': '値を入力してください'
        },
        numeric: {
            'default': '有効な浮動小数点数値を入力してください。'
        },
        phone: {
            'default': '有効な電話番号を入力してください',
            countryNotSupported: 'カントリーコード%sはサポートされていません',
            country: '有効な%sの電話番号を入力してください',
            countries: {
                BR: 'ブラジル',
                CN: 'チャイナ',
                CZ: 'チェコ共和国',
                DE: 'ドイツ',
                DK: 'デンマーク',
                ES: 'スペイン',
                FR: 'フランス',
                GB: 'イギリス',
                MA: 'モロッコ',
                PK: 'パキスタン',
                RO: 'ルーマニア',
                RU: 'ロシア',
                SK: 'スロバキア',
                TH: 'タイ国',
                US: 'アメリカ',
                VE: 'ベネズエラ'
            }
        },
        regexp: {
            'default': '正規表現に一致する値を入力してください'
        },
        remote: {
            'default': '有効な値を入力してください。'
        },
        rtn: {
            'default': '有効なRTN番号を入力してください'
        },
        sedol: {
            'default': '有効なSEDOL番号を入力してください'
        },
        siren: {
            'default': '有効なSIREN番号を入力してください'
        },
        siret: {
            'default': '有効なSIRET番号を入力してください'
        },
        step: {
            'default': '%sの有効なステップを入力してください'
        },
        stringCase: {
            'default': '小文字のみで入力してください',
            upper: '大文字のみで入力してください'
        },
        stringLength: {
            'default': '有効な長さの値を入力してください',
            less: '%s文字未満で入力してください',
            more: '%s文字より大きく入力してください',
            between: '%s文字から%s文字の間で入力してください'
        },
        uri: {
            'default': '有効なURIを入力してください。'
        },
        uuid: {
            'default': '有効なUUIDを入力してください',
            version: '有効なバージョン%s UUIDを入力してください'
        },
        vat: {
            'default': '有効なVAT番号を入力してください',
            countryNotSupported: 'カントリーコード%sはサポートされていません',
            country: '有効な%sのVAT番号を入力してください',
            countries: {
                AT: 'オーストリア',
                BE: 'ベルギー',
                BG: 'ブルガリア',
                BR: 'ブラジル',
                CH: 'スイス',
                CY: 'キプロス等',
                CZ: 'チェコ共和国',
                DE: 'ドイツ',
                DK: 'デンマーク',
                EE: 'エストニア',
                ES: 'スペイン',
                FI: 'フィンランド',
                FR: 'フランス',
                GB: 'イギリス',
                GR: 'ギリシャ',
                EL: 'ギリシャ',
                HU: 'ハンガリー',
                HR: 'クロアチア',
                IE: 'アイルランド',
                IS: 'アイスランド',
                IT: 'イタリア',
                LT: 'リトアニア',
                LU: 'ルクセンブルグ',
                LV: 'ラトビア',
                MT: 'マルタ',
                NL: 'オランダ',
                NO: 'ノルウェー',
                PL: 'ポーランド',
                PT: 'ポルトガル',
                RO: 'ルーマニア',
                RU: 'ロシア',
                RS: 'セルビア',
                SE: 'スウェーデン',
                SI: 'スロベニア',
                SK: 'スロバキア',
                VE: 'ベネズエラ',
                ZA: '南アフリカ'
            }
        },
        vin: {
            'default': '有効なVIN番号を入力してください'
        },
        zipCode: {
            'default': '有効な郵便番号を入力してください',
            countryNotSupported: 'カントリーコード%sはサポートされていません',
            country: '有効な%sの郵便番号を入力してください',
            countries: {
                AT: 'オーストリア',
                BR: 'ブラジル',
                CA: 'カナダ',
                CH: 'スイス',
                CZ: 'チェコ共和国',
                DE: 'ドイツ',
                DK: 'デンマーク',
                FR: 'フランス',
                GB: 'イギリス',
                IE: 'アイルランド',
                IT: 'イタリア',
                MA: 'モロッコ',
                NL: 'オランダ',
                PT: 'ポルトガル',
                RO: 'ルーマニア',
                RU: 'ロシア',
                SE: 'スウェーデン',
                SG: 'シンガポール',
                SK: 'スロバキア',
                US: 'アメリカ'
            }
        }
    });
}(window.jQuery));
