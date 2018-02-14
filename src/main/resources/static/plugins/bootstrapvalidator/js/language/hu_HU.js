(function($) {
    /**
     * Hungarian language package
     * Translated by @blackfyre
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Kérlek, hogy érvényes base 64 karakter láncot adj meg'
        },
        between: {
            'default': 'Kérlek, hogy %s és %s között adj meg értéket',
            notInclusive: 'Kérlek, hogy %s és %s között adj meg értéket'
        },
        callback: {
            'default': 'Kérlek, hogy érvényes értéket adj meg'
        },
        choice: {
            'default': 'Kérlek, hogy érvényes értéket adj meg',
            less: 'Kérlek, hogy legalább %s lehetőséget válassz ki',
            more: 'Kérlek, hogy maximum %s lehetőséget válassz ki',
            between: 'Kérlek, hogy válassz %s - %s lehetőséget'
        },
        color: {
            'default': 'Kérlek, hogy érvényes színt ajd meg'
        },
        creditCard: {
            'default': 'Kérlek, hogy érvényes bankkártya számot adj meg'
        },
        cusip: {
            'default': 'Kérlek, hogy érvényes CUSIP számot ajd meg'
        },
        cvv: {
            'default': 'Kérlek, hogy érvényes CVV számot ajd meg'
        },
        date: {
            'default': 'Kérlek, hogy érvényes dátumot ajd meg',
            min: 'Kérlek, hogy %s -nál későbbi dátumot adj meg',
            max: 'Kérlek, hogy %s -nál korábbi dátumot adj meg',
            range: 'Kérlek, hogy %s - %s között adj meg dátumot'
        },
        different: {
            'default': 'Kérlek, hogy egy másik értéket adj meg'
        },
        digits: {
            'default': 'Kérlek, hogy csak számot adj meg'
        },
        ean: {
            'default': 'Kérlek, hogy érvényes EAN számot ajd meg'
        },
        emailAddress: {
            'default': 'Kérlek, hogy érvényes email címet ajd meg'
        },
        file: {
            'default': 'Kérlek, hogy érvényes fájlt válassz'
        },
        greaterThan: {
            'default': 'Kérlek, hogy ezzel (%s) egyenlő vagy nagyobb számot adj meg',
            notInclusive: 'Kérlek, hogy ennél (%s) nagyobb számot adj meg'
        },
        grid: {
            'default': 'Kérlek, hogy érvényes GRId számot ajd meg'
        },
        hex: {
            'default': 'Kérlek, hogy érvényes hexadecimális számot ajd meg'
        },
        hexColor: {
            'default': 'Kérlek, hogy érvényes hexadecimális színt ajd meg'
        },
        iban: {
            'default': 'Kérlek, hogy érvényes IBAN számot ajd meg',
            countryNotSupported: 'A(z) %s országkód nem támogatott',
            country: 'Kérlek, hogy %s érvényes  IBAN számot adj meg',
            countries: {
                AD: 'az Andorrai Fejedelemségben', /* Special case */
                AE: 'az Egyesült Arab Emírségekben', /* Special case */
                AL: 'Albániában',
                AO: 'Angolában',
                AT: 'Ausztriában',
                AZ: 'Azerbajdzsánban',
                BA: 'Bosznia-Hercegovinában', /* Special case */
                BE: 'Belgiumban',
                BF: 'Burkina Fasoban',
                BG: 'Bulgáriában',
                BH: 'Bahreinben',
                BI: 'Burundiban',
                BJ: 'Beninben',
                BR: 'Brazíliában',
                CH: 'Svájcban',
                CI: 'az Elefántcsontparton', /* Special case */
                CM: 'Kamerunban',
                CR: 'Costa Ricán', /* Special case */
                CV: 'Zöld-foki Köztársaságban',
                CY: 'Cypruson',
                CZ: 'Csehországban',
                DE: 'Németországban',
                DK: 'Dániában',
                DO: 'Dominikán', /* Special case */
                DZ: 'Algériában',
                EE: 'Észtországban',
                ES: 'Spanyolországban',
                FI: 'Finnországban',
                FO: 'a Feröer-szigeteken', /* Special case */
                FR: 'Franciaországban',
                GB: 'az Egyesült Királyságban', /* Special case */
                GE: 'Grúziában',
                GI: 'Gibraltáron', /* Special case */
                GL: 'Grönlandon', /* Special case */
                GR: 'Görögországban',
                GT: 'Guatemalában',
                HR: 'Horvátországban',
                HU: 'Magyarországon',
                IE: 'Írországban', /* Special case */
                IL: 'Izraelben',
                IR: 'Iránban', /* Special case */
                IS: 'Izlandon',
                IT: 'Olaszországban',
                JO: 'Jordániában',
                KW: 'Kuvaitban', /* Special case */
                KZ: 'Kazahsztánban',
                LB: 'Libanonban',
                LI: 'Liechtensteinben',
                LT: 'Litvániában',
                LU: 'Luxemburgban',
                LV: 'Lettországban',
                MC: 'Monacóban', /* Special case */
                MD: 'Moldovában', /* Special case */
                ME: 'Montenegróban',
                MG: 'Madagaszkáron',
                MK: 'Macedóniában',
                ML: 'Malin',
                MR: 'Mauritániában',
                MT: 'Máltán',
                MU: 'Mauritiuson',
                MZ: 'Mozambikban',
                NL: 'Hollandiában',
                NO: 'Norvégiában',
                PK: 'Pakisztánban',
                PL: 'Lengyelországban',
                PS: 'Palesztinában',
                PT: 'Portugáliában',
                QA: 'Katarban', /* Special case */
                RO: 'Romániában',
                RS: 'Szerbiában',
                SA: 'Szaúd-Arábiában',
                SE: 'Svédországban',
                SI: 'Szlovéniában',
                SK: 'Szlovákiában',
                SM: 'San Marinoban',
                SN: 'Szenegálban',/* Special case */
                TN: 'Tunéziában',/* Special case */
                TR: 'Törökországban',
                VG: 'Britt Virgin szigeteken' /* Special case */
            }
        },
        id: {
            'default': 'Kérlek, hogy érvényes személy azonosító számot adj meg',
            countryNotSupported: 'A(z) %s országkód nem támogatott',
            country: 'Kérlek, hogy %s érvényes személy azonosító számot ajd meg',
            countries: {
                BA: 'Bosznia-Hercegovinában',
                BG: 'Bulgáriában',
                BR: 'Brazíliában',
                CH: 'Svájcban',
                CL: 'Chilében',
                CN: 'Kínában',
                CZ: 'Csehországban',
                DK: 'Dániában',
                EE: 'Észtországban',
                ES: 'Spanyolországban',
                FI: 'Finnországban',
                HR: 'Horvátországban',
                IE: 'Írországban',
                IS: 'Izlandon',
                LT: 'Litvániában',
                LV: 'Lettországban',
                ME: 'Montenegróban',
                MK: 'Macedóniában',
                NL: 'Hollandiában',
                RO: 'Romániában',
                RS: 'Szerbiában',
                SE: 'Svédországban',
                SI: 'Szlovéniában',
                SK: 'Szlovákiában',
                SM: 'San Marinoban',
                TH: 'Thaiföldön',
                ZA: 'Dél-Afrikában'
            }
        },
        identical: {
            'default': 'Kérlek, hogy ugyan azt az értéket add meg'
        },
        imei: {
            'default': 'Kérlek, hogy érvényes IMEI számot adj meg'
        },
        imo: {
            'default': 'Kérlek, hogy érvényes IMO számot adj meg'
        },
        integer: {
            'default': 'Kérlek, hogy számot adj meg'
        },
        ip: {
            'default': 'Kérlek, hogy IP címet adj meg',
            ipv4: 'Kérlek, hogy érvényes IPv4 címet ajd meg',
            ipv6: 'Kérlek, hogy érvényes IPv6 címet ajd meg'
        },
        isbn: {
            'default': 'Kérlek, hogy érvényes ISBN számot ajd meg'
        },
        isin: {
            'default': 'Kérlek, hogy érvényes ISIN számot ajd meg'
        },
        ismn: {
            'default': 'Kérlek, hogy érvényes ISMN számot ajd meg'
        },
        issn: {
            'default': 'Kérlek, hogy érvényes ISSN számot ajd meg'
        },
        lessThan: {
            'default': 'Kérlek, hogy adj meg egy számot ami kisebb vagy egyenlő mint %s',
            notInclusive: 'Kérlek, hogy adj meg egy számot ami kisebb mint %s'
        },
        mac: {
            'default': 'Kérlek, hogy érvényes MAC címet ajd meg'
        },
        meid: {
            'default': 'Kérlek, hogy érvényes MEID számot ajd meg'
        },
        notEmpty: {
            'default': 'Kérlek, hogy adj értéket a mezőnek'
        },
        numeric: {
            'default': 'Please enter a valid float number'
        },
        phone: {
            'default': 'Kérlek, hogy érvényes telefonszámot adj meg',
            countryNotSupported: 'A(z) %s országkód nem támogatott',
            country: 'Kérlek, hogy %s érvényes telefonszámot adj meg',
            countries: {
                BR: 'Brazíliában',
                CN: 'Kínában',
                CZ: 'Csehországban',
                DE: 'Németországban',
                DK: 'Dániában',
                ES: 'Spanyolországban',
                FR: 'Franciaországban',
                GB: 'az Egyesült Királyságban',
                MA: 'Marokkóban',
                PK: 'Pakisztánban',
                RO: 'Romániában',
                RU: 'Oroszországban',
                SK: 'Szlovákiában',
                TH: 'Thaiföldön',
                US: 'az Egyesült Államokban',
                VE: 'Venezuelában' /* Sepcial case */
            }
        },
        regexp: {
            'default': 'Kérlek, hogy a mintának megfelelő értéket adj meg'
        },
        remote: {
            'default': 'Kérlek, hogy érvényes értéket adj meg'
        },
        rtn: {
            'default': 'Kérlek, hogy érvényes RTN számot adj meg'
        },
        sedol: {
            'default': 'Kérlek, hogy érvényes SEDOL számot adj meg'
        },
        siren: {
            'default': 'Kérlek, hogy érvényes SIREN számot adj meg'
        },
        siret: {
            'default': 'Kérlek, hogy érvényes SIRET számot adj meg'
        },
        step: {
            'default': 'Kérlek, hogy érvényes lépteket adj meg (%s)'
        },
        stringCase: {
            'default': 'Kérlek, hogy csak kisbetüket ajd meg',
            upper: 'Kérlek, hogy csak nagy betüket adj meg'
        },
        stringLength: {
            'default': 'Kérlek, hogy érvényes karakter hosszúsággal adj meg értéket',
            less: 'Kérlek, hogy kevesebb mint %s karaktert adj meg',
            more: 'Kérlek, hogy több mint %s karaktert adj meg',
            between: 'Kérlek, hogy legalább %s, de maximum %s karaktert adj meg'
        },
        uri: {
            'default': 'Kérlek, hogy helyes URI -t adj meg'
        },
        uuid: {
            'default': 'Kérlek, hogy érvényes UUID számot adj meg',
            version: 'Kérlek, hogy érvényes UUID verzió %s számot adj meg'
        },
        vat: {
            'default': 'Kérlek, hogy helyes adó számot adj meg',
            countryNotSupported: '%s - nem támogatott ország',
            country: 'Kérlek, hogy %s helyes  adószámot ajd meg',
            countries: {
                AT: 'Ausztriában',
                BE: 'Belgiumban',
                BG: 'Bulgáriában',
                BR: 'Brazíliában',
                CH: 'Svájcban',
                CY: 'Cipruson',
                CZ: 'Csehországban',
                DE: 'Németországban',
                DK: 'Dániában',
                EE: 'Észtországban',
                ES: 'Spanyolországban',
                FI: 'Finnországban',
                FR: 'Franciaországban',
                GB: 'az Egyesült Királyságban',
                GR: 'Görögországban',
                EL: 'Görögországban',
                HU: 'Magyarországon',
                HR: 'Horvátországban',
                IE: 'Írországban',
                IS: 'Izlandon',
                IT: 'Olaszországban',
                LT: 'Litvániában',
                LU: 'Luxemburgban',
                LV: 'Lettországban',
                MT: 'Máltán',
                NL: 'Hollandiában',
                NO: 'Norvégiában',
                PL: 'Lengyelországban',
                PT: 'Portugáliában',
                RO: 'Romániában',
                RU: 'Oroszországban',
                RS: 'Szerbiában',
                SE: 'Svédországban',
                SI: 'Szlovéniában',
                SK: 'Szlovákiában',
                VE: 'Venezuelában',
                ZA: 'Dél-Afrikában'
            }
        },
        vin: {
            'default': 'Kérlek, hogy érvényes VIN számot adj meg'
        },
        zipCode: {
            'default': 'Kérlek, hogy érvényes irányítószámot adj meg',
            countryNotSupported: '%s - nem támogatott ország',
            country: 'Kérlek, hogy %s érvényes irányítószámot adj meg',
            countries: {
                AT: 'Ausztriában',
                BR: 'Brazíliában',
                CA: 'Kanadában',
                CH: 'Svájcban',
                CZ: 'Csehországban',
                DE: 'Németországban',
                DK: 'Dániában',
                FR: 'Franciaországban',
                GB: 'az Egyesült Királyságban',
                IE: 'Írországban',
                IT: 'Olaszországban',
                MA: 'Marokkóban',
                NL: 'Hollandiában',
                PT: 'Portugáliában',
                RO: 'Romániában',
                RU: 'Oroszországban',
                SE: 'Svájcban',
                SG: 'Szingapúrban',
                SK: 'Szlovákiában',
                US: 'Egyesült Államok beli'
            }
        }
    });
}(window.jQuery));
