(function($) {
    /**
     * The Dutch language package
     * Translated by @jvanderheide
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Voer een geldige Base64 geëncodeerde tekst in'
        },
        between: {
            'default': 'Voer een waarde in van %s tot en met %s',
            notInclusive: 'Voer een waarde die tussen %s en %s ligt'
        },
        callback: {
            'default': 'Voer een geldige waarde in'
        },
        choice: {
            'default': 'Voer een geldige waarde in',
            less: 'Kies minimaal %s optie(s)',
            more: 'Kies maximaal %s opties',
            between: 'Kies tussen de %s - %s opties'
        },
        color: {
            'default': 'Voer een geldige kleurcode in'
        },
        creditCard: {
            'default': 'Voer een geldig creditcardnummer in'
        },
        cusip: {
            'default': 'Voer een geldig CUSIP-nummer in'
        },
        cvv: {
            'default': 'Voer een geldig CVV-nummer in'
        },
        date: {
            'default': 'Voer een geldige datum in',
            min: 'Voer een datum in die na %s ligt',
            max: 'Voer een datum in die vóór %s ligt',
            range: 'Voer een datum in die tussen %s en %s ligt'
        },
        different: {
            'default': 'Voer een andere waarde in'
        },
        digits: {
             'default': 'Voer enkel cijfers in'
        },
        ean: {
            'default': 'Voer een geldige EAN-code in'
        },
        emailAddress: {
            'default': 'Voer een geldig e-mailadres in'
        },
        file: {
            'default': 'Kies een geldig bestand'
        },
        greaterThan: {
            'default': 'Voer een waarde in die gelijk is aan of groter is dan %s',
            notInclusive: 'Voer een waarde in die is groter dan %s'
        },
        grid: {
            'default': 'Voer een geldig GRId-nummer in'
        },
        hex: {
            'default': 'Voer een geldig hexadecimaal nummer in'
        },
        hexColor: {
            'default': 'Voer een geldige hexadecimale kleurcode in'
        },
        iban: {
            'default': 'Voer een geldig IBAN nummer in',
            countryNotSupported: 'De land code %s wordt niet ondersteund',
            country: 'Voer een geldig IBAN nummer in uit %s',
            countries: {
                AD: 'Andorra',
                AE: 'Verenigde Arabische Emiraten',
                AL: 'Albania',
                AO: 'Angola',
                AT: 'Oostenrijk',
                AZ: 'Azerbeidzjan',
                BA: 'Bosnië en Herzegovina',
                BE: 'België',
                BF: 'Burkina Faso',
                BG: 'Bulgarije"',
                BH: 'Bahrein',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brazilië',
                CH: 'Zwitserland',
                CI: 'Ivoorkust',
                CM: 'Kameroen',
                CR: 'Costa Rica',
                CV: 'Cape Verde',
                CY: 'Cyprus',
                CZ: 'Tsjechische Republiek',
                DE: 'Duitsland',
                DK: 'Denemarken',
                DO: 'Dominicaanse Republiek',
                DZ: 'Algerije',
                EE: 'Estland',
                ES: 'Spanje',
                FI: 'Finland',
                FO: 'Faeröer',
                FR: 'Frankrijk',
                GB: 'Verenigd Koninkrijk',
                GE: 'Georgia',
                GI: 'Gibraltar',
                GL: 'Groenland',
                GR: 'Griekenland',
                GT: 'Guatemala',
                HR: 'Kroatië',
                HU: 'Hongarije',
                IE: 'Ierland',
                IL: 'Israël',
                IR: 'Iran',
                IS: 'IJsland',
                IT: 'Italië',
                JO: 'Jordan',
                KW: 'Koeweit',
                KZ: 'Kazachstan',
                LB: 'Libanon',
                LI: 'Liechtenstein',
                LT: 'Litouwen',
                LU: 'Luxemburg',
                LV: 'Letland',
                MC: 'Monaco',
                MD: 'Moldavië',
                ME: 'Montenegro',
                MG: 'Madagascar',
                MK: 'Macedonië',
                ML: 'Mali',
                MR: 'Mauretanië',
                MT: 'Malta',
                MU: 'Mauritius',
                MZ: 'Mozambique',
                NL: 'Nederland',
                NO: 'Noorwegen',
                PK: 'Pakistan',
                PL: 'Polen',
                PS: 'Palestijnse',
                PT: 'Portugal',
                QA: 'Qatar',
                RO: 'Roemenië',
                RS: 'Servië',
                SA: 'Saudi-Arabië',
                SE: 'Zweden',
                SI: 'Slovenië',
                SK: 'Slowakije',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunesië',
                TR: 'Turkije',
                VG: 'Britse Maagdeneilanden'
            }
        },
        id: {
            'default': 'Voer een geldig identificatie nummer in',
            countryNotSupported: 'De land code %s wordt niet ondersteund',
            country: 'Voer een geldig identificatie nummer in uit %s',
            countries: {
                BA: 'Bosnië en Herzegovina',
                BG: 'Bulgarije',
                BR: 'Brazilië',
                CH: 'Zwitserland',
                CL: 'Chili',
                CN: 'China',
                CZ: 'Tsjechische Republiek',
                DK: 'Denemarken',
                EE: 'Estland',
                ES: 'Spanje',
                FI: 'Finland',
                HR: 'Kroatië',
                IE: 'Ierland',
                IS: 'IJsland',
                LT: 'Litouwen',
                LV: 'Letland',
                ME: 'Montenegro',
                MK: 'Macedonië',
                NL: 'Nederland',
                RO: 'Roemenië',
                RS: 'Servië',
                SE: 'Zweden',
                SI: 'Slovenië',
                SK: 'Slowakije',
                SM: 'San Marino',
                TH: 'Thailand',
                ZA: 'Zuid-Afrika'
            }
        },
        identical: {
            'default': 'Voer dezelfde waarde in'
        },
        imei: {
            'default': 'Voer een geldig IMEI-nummer in'
        },
        imo: {
            'default': 'Voer een geldig IMO-nummer in'
        },
        integer: {
            'default': 'Voer een geldig getal in'
        },
        ip: {
            'default': 'Voer een geldig IP adres in',
            ipv4: 'Voer een geldig IPv4 adres in',
            ipv6: 'Voer een geldig IPv6 adres in'
        },
        isbn: {
            'default': 'Voer een geldig ISBN-nummer in'
        },
        isin: {
            'default': 'Voer een geldig ISIN-nummer in'
        },
        ismn: {
            'default': 'Voer een geldig ISMN-nummer in'
        },
        issn: {
            'default': 'Voer een geldig ISSN-nummer in'
        },
        lessThan: {
            'default': 'Voer een waarde in gelijk aan of kleiner dan %s',
            notInclusive: 'Voer een waarde in kleiner dan %s'
        },
        mac: {
            'default': 'Voer een geldig MAC adres in'
        },
        meid: {
            'default': 'Voer een geldig MEID-nummer in'
        },
        notEmpty: {
            'default': 'Voer een waarde in'
        },
        numeric: {
            'default': 'Voer een geldig kommagetal in'
        },
        phone: {
            'default': 'Voer een geldig telefoonnummer in',
            countryNotSupported: 'De land code %s wordt niet ondersteund',
            country: 'Voer een geldig telefoonnummer in uit %s',
            countries: {
                BR: 'Brazilië',
                CN: 'China',
                CZ: 'Tsjechische Republiek',
                DE: 'Duitsland',
                DK: 'Denemarken',
                ES: 'Spanje',
                FR: 'Frankrijk',
                GB: 'Verenigd Koninkrijk',
                MA: 'Marokko',
                PK: 'Pakistan',
                RO: 'Roemenië',
                RU: 'Rusland',
                SK: 'Slowakije',
                TH: 'Thailand',
                US: 'VS',
                VE: 'Venezuela'
            }
        },
        regexp: {
            'default': 'Voer een waarde in die overeenkomt met het patroon'
        },
        remote: {
            'default': 'Voer een geldige waarde in'
        },
        rtn: {
            'default': 'Voer een geldig RTN-nummer in'
        },
        sedol: {
            'default': 'Voer een geldig SEDOL-nummer in'
        },
        siren: {
            'default': 'Voer een geldig SIREN-nummer in'
        },
        siret: {
            'default': 'Voer een geldig SIRET-nummer in'
        },
        step: {
            'default': 'Voer een meervoud van %s in'
        },
        stringCase: {
            'default': 'Voer enkel kleine letters in',
            upper: 'Voer enkel hoofdletters in'
        },
        stringLength: {
            'default': 'Voer een waarde met de juiste lengte in',
            less: 'Voer minder dan %s karakters in',
            more: 'Voer meer dan %s karakters in',
            between: 'Voer tussen tussen %s en %s karakters in'
        },
        uri: {
            'default': 'Voer een geldige link in'
        },
        uuid: {
            'default': 'Voer een geldige UUID in',
            version: 'Voer een geldige UUID (versie %s) in'
        },
        vat: {
            'default': 'Voer een geldig BTW-nummer in',
            countryNotSupported: 'De land code %s wordt niet ondersteund',
            country: 'Voer een geldig BTW-nummer in uit %s',
            countries: {
                AT: 'Oostenrijk',
                BE: 'België',
                BG: 'Bulgarije',
                BR: 'Brazilië',
                CH: 'Zwitserland',
                CY: 'Cyprus',
                CZ: 'Tsjechische Republiek',
                DE: 'Duitsland',
                DK: 'Denemarken',
                EE: 'Estland',
                ES: 'Spanje',
                FI: 'Finland',
                FR: 'Frankrijk',
                GB: 'Verenigd Koninkrijk',
                GR: 'Griekenland',
                EL: 'Griekenland',
                HU: 'Hongarije',
                HR: 'Kroatië',
                IE: 'Ierland',
                IS: 'IJsland',
                IT: 'Italië',
                LT: 'Litouwen',
                LU: 'Luxemburg',
                LV: 'Letland',
                MT: 'Malta',
                NL: 'Nederland',
                NO: 'Noorwegen',
                PL: 'Polen',
                PT: 'Portugal',
                RO: 'Roemenië',
                RU: 'Rusland',
                RS: 'Servië',
                SE: 'Zweden',
                SI: 'Slovenië',
                SK: 'Slowakije',
                VE: 'Venezuela',
                ZA: 'Zuid-Afrika'
            }
        },
        vin: {
            'default': 'Voer een geldig VIN-nummer in'
        },
        zipCode: {
            'default': 'Voer een geldige postcode in',
            countryNotSupported: 'De land code %s wordt niet ondersteund',
            country: 'Voer een geldige postcode in uit %s',
            countries: {
                AT: 'Oostenrijk',
                BR: 'Brazilië',
                CA: 'Canada',
                CH: 'Zwitserland',
                CZ: 'Tsjechische Republiek',
                DE: 'Duitsland',
                DK: 'Denemarken',
                FR: 'Frankrijk',
                GB: 'Verenigd Koninkrijk',
                IE: 'Ierland',
                IT: 'Italië',
                MA: 'Marokko',
                NL: 'Nederland',
                PT: 'Portugal',
                RO: 'Roemenië',
                RU: 'Rusland',
                SE: 'Zweden',
                SG: 'Singapore',
                SK: 'Slowakije',
                US: 'VS'
            }
        }
    });
}(window.jQuery));
