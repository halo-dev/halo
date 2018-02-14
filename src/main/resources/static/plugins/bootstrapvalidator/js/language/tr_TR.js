(function($) {
    /**
     * Turkish language package
     * Translated By @CeRBeR666
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Lütfen 64 bit tabanına uygun bir giriş yapınız'
        },
        between: {
            'default': 'Lütfen %s ile %s arasında bir değer giriniz',
            notInclusive: 'Lütfen sadece %s ile %s arasında bir değer giriniz'
        },
        callback: {
            'default': 'Lütfen geçerli bir değer giriniz'
        },
        choice: {
            'default': 'Lütfen geçerli bir değer giriniz',
            less: 'Lütfen minimum %s kadar değer giriniz',
            more: 'Lütfen maksimum %s kadar değer giriniz',
            between: 'Lütfen %s - %s arası seçiniz'
        },
        color: {
            'default': 'Lütfen geçerli bir codu giriniz'
        },
        creditCard: {
            'default': 'Lütfen geçerli bir kredi kartı numarası giriniz'
        },
        cusip: {
            'default': 'Lütfen geçerli bir CUSIP numarası giriniz'
        },
        cvv: {
            'default': 'Lütfen geçerli bir CCV numarası giriniz'
        },
        date: {
            'default': 'Lütfen geçerli bir tarih giriniz',
            min: 'Lütfen %s tarihinden sonra bir tarih giriniz',
            max: 'Lütfen %s tarihinden önce bir tarih giriniz',
            range: 'Lütfen %s - %s aralığında bir tarih giriniz'
        },
        different: {
            'default': 'Lütfen farklı bir değer giriniz'
        },
        digits: {
             'default': 'Lütfen sadece sayı giriniz'
        },
        ean: {
            'default': 'Lütfen geçerli bir EAN numarası giriniz'
        },
        emailAddress: {
            'default': 'Lütfen geçerli bir E-Mail adresi giriniz'
        },
        file: {
            'default': 'Lütfen geçerli bir dosya seçiniz'
        },
        greaterThan: {
            'default': 'Lütfen %s ye eşit veya daha büyük bir değer giriniz',
            notInclusive: 'Lütfen %s den büyük bir değer giriniz'
        },
        grid: {
            'default': 'Lütfen geçerli bir GRId numarası giriniz'
        },
        hex: {
            'default': 'Lütfen geçerli bir Hexadecimal sayı giriniz'
        },
        hexColor: {
            'default': 'Lütfen geçerli bir HEX codu giriniz'
        },
        iban: {
            'default': 'Lütfen geçerli bir IBAN numarası giriniz',
            countryNotSupported: '%s ülke kodu desteklenmemektedir',
            country: 'Lütfen geçerli bir IBAN numarası giriniz içinde %s',
            countries: {
                AD: 'Andorra',
                AE: 'Birleşik Arap Emirlikleri',
                AL: 'Arnavutluk',
                AO: 'Angola',
                AT: 'Avusturya',
                AZ: 'Azerbaycan',
                BA: 'Bosna Hersek',
                BE: 'Belçika',
                BF: 'Burkina Faso',
                BG: 'Bulgaristan',
                BH: 'Bahreyn',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brezilya',
                CH: 'İsviçre',
                CI: 'Fildişi Sahili',
                CM: 'Kamerun',
                CR: 'Kosta Rika',
                CV: 'Cape Verde',
                CY: 'Kıbrıs',
                CZ: 'Çek Cumhuriyeti',
                DE: 'Almanya',
                DK: 'Danimarka',
                DO: 'Dominik Cumhuriyeti',
                DZ: 'Cezayir',
                EE: 'Estonya',
                ES: 'İspanya',
                FI: 'Finlandiya',
                FO: 'Faroe Adaları',
                FR: 'Fransa',
                GB: 'İngiltere',
                GE: 'Georgia',
                GI: 'Cebelitarık',
                GL: 'Grönland',
                GR: 'Yunansitan',
                GT: 'Guatemala',
                HR: 'Hırvatistan',
                HU: 'Macaristan',
                IE: 'İrlanda',
                IL: 'İsrail',
                IR: 'İran',
                IS: 'İzlanda',
                IT: 'İtalya',
                JO: 'Ürdün',
                KW: 'Kuveit',
                KZ: 'Kazakistan',
                LB: 'Lübnan',
                LI: 'Lihtenştayn',
                LT: 'Litvanya',
                LU: 'Lüksemburg',
                LV: 'Letonya',
                MC: 'Monako',
                MD: 'Moldova',
                ME: 'Karadağ',
                MG: 'Madagaskar',
                MK: 'Makedonya',
                ML: 'Mali',
                MR: 'Moritanya',
                MT: 'Malta',
                MU: 'Mauritius',
                MZ: 'Mozambik',
                NL: 'Hollanda',
                NO: 'Norveç',
                PK: 'Pakistan',
                PL: 'Polanya',
                PS: 'Filistin',
                PT: 'Portekiz',
                QA: 'Katar',
                RO: 'Romanya',
                RS: 'Serbistan',
                SA: 'Suudi Arabistan',
                SE: 'İsveç',
                SI: 'Slovenya',
                SK: 'Slovakya',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunus',
                TR: 'Turkiye',
                VG: 'Virgin Adaları, İngiliz'
            }
        },
        id: {
            'default': 'Lütfen geçerli bir tanımlama numarası giriniz',
            countryNotSupported: '%s ülke kodu desteklenmiyor',
            country: 'Lütfen geçerli bir kimlik numarası giriniz içinde %s',
            countries: {
                BA: 'Bosna Hersek',
                BG: 'Bulgaristan',
                BR: 'Brezilya',
                CH: 'İsviçre',
                CL: 'Şili',
                CN: 'Çin',
                CZ: 'Çek Cumhuriyeti',
                DK: 'Danimarka',
                EE: 'Estonya',
                ES: 'İspanya',
                FI: 'Finlandiya',
                HR: 'Hırvatistan',
                IE: 'İrlanda',
                IS: 'İzlanda',
                LT: 'Litvanya',
                LV: 'Letonya',
                ME: 'Karadağ',
                MK: 'Makedonya',
                NL: 'Hollanda',
                RO: 'Romanya',
                RS: 'Sırbistan',
                SE: 'İsveç',
                SI: 'Slovenya',
                SK: 'Slovakya',
                SM: 'San Marino',
                TH: 'Tayland',
                ZA: 'Güney Afrika'
            }
        },
        identical: {
            'default': 'Lütfen aynı değeri giriniz'
        },
        imei: {
            'default': 'Lütfen geçerli bir IMEI numarası giriniz'
        },
        imo: {
            'default': 'Lütfen geçerli bir IMO numarası giriniz'
        },
        integer: {
            'default': 'Lütfen geçerli bir numara giriniz'
        },
        ip: {
            'default': 'Lütfen geçerli bir IP adresi giriniz',
            ipv4: 'Lütfen geçerli bir IPv4 adresi giriniz',
            ipv6: 'Lütfen geçerli bri IPv6 adresi giriniz'
        },
        isbn: {
            'default': 'Lütfen geçerli bir ISBN numarası giriniz'
        },
        isin: {
            'default': 'Lütfen geçerli bir ISIN numarası giriniz'
        },
        ismn: {
            'default': 'Lütfen geçerli bir ISMN numarası giriniz'
        },
        issn: {
            'default': 'Lütfen geçerli bir ISSN numarası giriniz'
        },
        lessThan: {
            'default': 'Lütfen %s den düşük veya eşit bir değer giriniz',
            notInclusive: 'Lütfen %s den büyük bir değer giriniz'
        },
        mac: {
            'default': 'Lütfen geçerli bir MAC Adresi giriniz'
        },
        meid: {
            'default': 'Lütfen geçerli bir MEID numarası giriniz'
        },
        notEmpty: {
            'default': 'Bir değer giriniz'
        },
        numeric: {
            'default': 'Lütfen geçerli bir float değer giriniz'
        },
        phone: {
            'default': 'Lütfen geçerli bir telefon numarası giriniz',
            countryNotSupported: '%s ülke kodu desteklenmemektedir',
            country: 'Lütfen geçerli bir telefon numarası giriniz içinde %s',
            countries: {
                BR: 'Brezilya',
                CN: 'Çin',
                CZ: 'Çek Cumhuriyeti',
                DE: 'Almanya',
                DK: 'Danimarka',
                ES: 'İspanya',
                FR: 'Fransa',
                GB: 'İngiltere',
                MA: 'Fas',
                PK: 'Pakistan',
                RO: 'Romanya',
                RU: 'Rusya',
                SK: 'Slovakya',
                TH: 'Tayland',
                US: 'Amerika',
                VE: 'Venezüella'
            }
        },
        regexp: {
            'default': 'Lütfen uyumlu bir değer giriniz'
        },
        remote: {
            'default': 'Lütfen geçerli bir numara giriniz'
        },
        rtn: {
            'default': 'Lütfen geçerli bir RTN numarası giriniz'
        },
        sedol: {
            'default': 'Lütfen geçerli bir SEDOL numarası giriniz'
        },
        siren: {
            'default': 'Lütfen geçerli bir SIREN numarası giriniz'
        },
        siret: {
            'default': 'Lütfen geçerli bir SIRET numarası giriniz'
        },
        step: {
            'default': 'Lütfen geçerli bir %s adımı giriniz'
        },
        stringCase: {
            'default': 'Lütfen sadece küçük harf giriniz',
            upper: 'Lütfen sadece büyük harf giriniz'
        },
        stringLength: {
            'default': 'Lütfen geçerli uzunluktaki bir değer giriniz',
            less: 'Lütfen %s karakterden az değer giriniz',
            more: 'Lütfen %s karakterden fazla değer giriniz',
            between: 'Lütfen %s ile %s arası uzunlukta bir değer giriniz'
        },
        uri: {
            'default': 'Lütfen geçerli bir URL giriniz'
        },
        uuid: {
            'default': 'Lütfen geçerli bir UUID numarası giriniz',
            version: 'Lütfen geçerli bir UUID versiyon %s numarası giriniz'
        },
        vat: {
            'default': 'Lütfen geçerli bir VAT kodu giriniz',
            countryNotSupported: '%s ülke kodu desteklenmiyor',
            country: 'Lütfen geçerli bir vergi numarası giriniz içinde %s',
            countries: {
                AT: 'Avustralya',
                BE: 'Belçika',
                BG: 'Bulgaristan',
                BR: 'Brezilya',
                CH: 'İsviçre',
                CY: 'Kıbrıs',
                CZ: 'Çek Cumhuriyeti',
                DE: 'Almanya',
                DK: 'Danimarka',
                EE: 'Estonya',
                ES: 'İspanya',
                FI: 'Finlandiya',
                FR: 'Fransa',
                GB: 'İngiltere',
                GR: 'Yunanistan',
                EL: 'Yunanistan',
                HU: 'Macaristan',
                HR: 'Hırvatistan',
                IE: 'Irlanda',
                IS: 'İzlanda',
                IT: 'Italya',
                LT: 'Litvanya',
                LU: 'Lüksemburg',
                LV: 'Letonya',
                MT: 'Malta',
                NL: 'Hollanda',
                NO: 'Norveç',
                PL: 'Polonya',
                PT: 'Portekiz',
                RO: 'Romanya',
                RU: 'Rusya',
                RS: 'Sırbistan',
                SE: 'İsveç',
                SI: 'Slovenya',
                SK: 'Slovakya',
                VE: 'Venezüella',
                ZA: 'Güney Afrika'
            }
        },
        vin: {
            'default': 'Lütfen geçerli bir VIN numarası giriniz'
        },
        zipCode: {
            'default': 'Lütfen geçerli bir posta kodu giriniz',
            countryNotSupported: '%s ülke kodu desteklenmemektedir',
            country: 'Lütfen geçerli bir posta kodu giriniz içinde %s',
            countries: {
                AT: 'Avustralya',
                BR: 'Brezilya',
                CA: 'Kanada',
                CH: 'İsviçre',
                CZ: 'Çek Cumhuriyeti',
                DE: 'Almanya',
                DK: 'Danimarka',
                FR: 'Fransa',
                GB: 'İngiltere',
                IE: 'Irlanda',
                IT: 'İtalya',
                MA: 'Fas',
                NL: 'Hollanda',
                PT: 'Portekiz',
                RO: 'Romanya',
                RU: 'Rusya',
                SE: 'İsveç',
                SG: 'Singapur',
                SK: 'Slovakya',
                US: 'Amerika Birleşik Devletleri'
            }
        }
    });
}(window.jQuery));
