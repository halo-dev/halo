(function($) {
    /**
     * Swedish language package
     * Translated by @ulsa
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Vänligen mata in ett giltigt Base64-kodat värde'
        },
        between: {
            'default': 'Vänligen mata in ett värde mellan %s och %s',
            notInclusive: 'Vänligen mata in ett värde strikt mellan %s och %s'
        },
        callback: {
            'default': 'Vänligen mata in ett giltigt värde'
        },
        choice: {
            'default': 'Vänligen mata in ett giltigt värde',
            less: 'Vänligen välj minst %s alternativ',
            more: 'Vänligen välj max %s alternativ',
            between: 'Vänligen välj %s - %s alternativ'
        },
        color: {
            'default': 'Vänligen mata in en giltig färg'
        },
        creditCard: {
            'default': 'Vänligen mata in ett giltigt kredikortsnummer'
        },
        cusip: {
            'default': 'Vänligen mata in ett giltigt CUSIP-nummer'
        },
        cvv: {
            'default': 'Vänligen mata in ett giltigt CVV-nummer'
        },
        date: {
            'default': 'Vänligen mata in ett giltigt datum',
            min: 'Vänligen mata in ett datum efter %s',
            max: 'Vänligen mata in ett datum före %s',
            range: 'Vänligen mata in ett datum i intervallet %s - %s'
        },
        different: {
            'default': 'Vänligen mata in ett annat värde'
        },
        digits: {
             'default': 'Vänligen mata in endast siffror'
        },
        ean: {
            'default': 'Vänligen mata in ett giltigt EAN-nummer'
        },
        emailAddress: {
            'default': 'Vänligen mata in en giltig emailadress'
        },
        file: {
            'default': 'Vänligen välj en giltig fil'
        },
        greaterThan: {
            'default': 'Vänligen mata in ett värde större än eller lika med %s',
            notInclusive: 'Vänligen mata in ett värde större än %s'
        },
        grid: {
            'default': 'Vänligen mata in ett giltigt GRID-nummer'
        },
        hex: {
            'default': 'Vänligen mata in ett giltigt hexadecimalt tal'
        },
        hexColor: {
            'default': 'Vänligen mata in en giltig hexadecimal färg'
        },
        iban: {
            'default': 'Vänligen mata in ett giltigt IBAN-nummer',
            countryNotSupported: 'Landskoden %s stöds inte',
            country: 'Vänligen mata in ett giltigt IBAN-nummer i %s',
            countries: {
                AD: 'Andorra',
                AE: 'Förenade Arabemiraten',
                AL: 'Albanien',
                AO: 'Angola',
                AT: 'Österrike',
                AZ: 'Azerbadjan',
                BA: 'Bosnien och Herzegovina',
                BE: 'Belgien',
                BF: 'Burkina Faso',
                BG: 'Bulgarien',
                BH: 'Bahrain',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brasilien',
                CH: 'Schweiz',
                CI: 'Elfenbenskusten',
                CM: 'Kamerun',
                CR: 'Costa Rica',
                CV: 'Cape Verde',
                CY: 'Cypern',
                CZ: 'Tjeckien',
                DE: 'Tyskland',
                DK: 'Danmark',
                DO: 'Dominikanska Republiken',
                DZ: 'Algeriet',
                EE: 'Estland',
                ES: 'Spanien',
                FI: 'Finland',
                FO: 'Färöarna',
                FR: 'Frankrike',
                GB: 'Storbritannien',
                GE: 'Georgien',
                GI: 'Gibraltar',
                GL: 'Grönland',
                GR: 'Greekland',
                GT: 'Guatemala',
                HR: 'Kroatien',
                HU: 'Ungern',
                IE: 'Irland',
                IL: 'Israel',
                IR: 'Iran',
                IS: 'Island',
                IT: 'Italien',
                JO: 'Jordanien',
                KW: 'Kuwait',
                KZ: 'Kazakstan',
                LB: 'Libanon',
                LI: 'Lichtenstein',
                LT: 'Litauen',
                LU: 'Luxemburg',
                LV: 'Lettland',
                MC: 'Monaco',
                MD: 'Moldovien',
                ME: 'Montenegro',
                MG: 'Madagaskar',
                MK: 'Makedonien',
                ML: 'Mali',
                MR: 'Mauretanien',
                MT: 'Malta',
                MU: 'Mauritius',
                MZ: 'Mozambique',
                NL: 'Holland',
                NO: 'Norge',
                PK: 'Pakistan',
                PL: 'Polen',
                PS: 'Palestina',
                PT: 'Portugal',
                QA: 'Qatar',
                RO: 'Rumänien',
                RS: 'Serbien',
                SA: 'Saudiarabien',
                SE: 'Sverige',
                SI: 'Slovenien',
                SK: 'Slovakien',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunisien',
                TR: 'Turkiet',
                VG: 'Brittiska Jungfruöarna'
            }
        },
        id: {
            'default': 'Vänligen mata in ett giltigt identifikationsnummer',
            countryNotSupported: 'Landskoden %s stöds inte',
            country: 'Vänligen mata in ett giltigt identifikationsnummer i %s',
            countries: {
                BA: 'Bosnien och Hercegovina',
                BG: 'Bulgarien',
                BR: 'Brasilien',
                CH: 'Schweiz',
                CL: 'Chile',
                CN: 'Kina',
                CZ: 'Tjeckien',
                DK: 'Danmark',
                EE: 'Estland',
                ES: 'Spanien',
                FI: 'Finland',
                HR: 'Kroatien',
                IE: 'Irland',
                IS: 'Island',
                LT: 'Litauen',
                LV: 'Lettland',
                ME: 'Montenegro',
                MK: 'Makedonien',
                NL: 'Nederländerna',
                RO: 'Rumänien',
                RS: 'Serbien',
                SE: 'Sverige',
                SI: 'Slovenien',
                SK: 'Slovakien',
                SM: 'San Marino',
                TH: 'Thailand',
                ZA: 'Sydafrika'
            }
        },
        identical: {
            'default': 'Vänligen mata in samma värde'
        },
        imei: {
            'default': 'Vänligen mata in ett giltigt IMEI-nummer'
        },
        imo: {
            'default': 'Vänligen mata in ett giltigt IMO-nummer'
        },
        integer: {
            'default': 'Vänligen mata in ett giltigt heltal'
        },
        ip: {
            'default': 'Vänligen mata in en giltig IP-adress',
            ipv4: 'Vänligen mata in en giltig IPv4-adress',
            ipv6: 'Vänligen mata in en giltig IPv6-adress'
        },
        isbn: {
            'default': 'Vänligen mata in ett giltigt ISBN-nummer'
        },
        isin: {
            'default': 'Vänligen mata in ett giltigt ISIN-nummer'
        },
        ismn: {
            'default': 'Vänligen mata in ett giltigt ISMN-nummer'
        },
        issn: {
            'default': 'Vänligen mata in ett giltigt ISSN-nummer'
        },
        lessThan: {
            'default': 'Vänligen mata in ett värde mindre än eller lika med %s',
            notInclusive: 'Vänligen mata in ett värde mindre än %s'
        },
        mac: {
            'default': 'Vänligen mata in en giltig MAC-adress'
        },
        meid: {
            'default': 'Vänligen mata in ett giltigt MEID-nummer'
        },
        notEmpty: {
            'default': 'Vänligen mata in ett värde'
        },
        numeric: {
            'default': 'Vänligen mata in ett giltigt flyttal'
        },
        phone: {
            'default': 'Vänligen mata in ett giltigt telefonnummer',
            countryNotSupported: 'Landskoden %s stöds inte',
            country: 'Vänligen mata in ett giltigt telefonnummer i %s',
            countries: {
                BR: 'Brasilien',
                CN: 'Kina',
                CZ: 'Tjeckien',
                DE: 'Tyskland',
                DK: 'Danmark',
                ES: 'Spanien',
                FR: 'Frankrike',
                GB: 'Storbritannien',
                MA: 'Marocko',
                PK: 'Pakistan',
                RO: 'Rumänien',
                RU: 'Ryssland',
                SK: 'Slovakien',
                TH: 'Thailand',
                US: 'USA',
                VE: 'Venezuela'
            }
        },
        regexp: {
            'default': 'Vänligen mata in ett värde som matchar uttrycket'
        },
        remote: {
            'default': 'Vänligen mata in ett giltigt värde'
        },
        rtn: {
            'default': 'Vänligen mata in ett giltigt RTN-nummer'
        },
        sedol: {
            'default': 'Vänligen mata in ett giltigt SEDOL-nummer'
        },
        siren: {
            'default': 'Vänligen mata in ett giltigt SIREN-nummer'
        },
        siret: {
            'default': 'Vänligen mata in ett giltigt SIRET-nummer'
        },
        step: {
            'default': 'Vänligen mata in ett giltigt steg av %s'
        },
        stringCase: {
            'default': 'Vänligen mata in endast små bokstäver',
            upper: 'Vänligen mata in endast stora bokstäver'
        },
        stringLength: {
            'default': 'Vänligen mata in ett värde med giltig längd',
            less: 'Vänligen mata in färre än %s tecken',
            more: 'Vänligen mata in fler än %s tecken',
            between: 'Vänligen mata in ett värde mellan %s och %s tecken långt'
        },
        uri: {
            'default': 'Vänligen mata in en giltig URI'
        },
        uuid: {
            'default': 'Vänligen mata in ett giltigt UUID-nummer',
            version: 'Vänligen mata in ett giltigt UUID-nummer av version %s'
        },
        vat: {
            'default': 'Vänligen mata in ett giltigt momsregistreringsnummer',
            countryNotSupported: 'Landskoden %s stöds inte',
            country: 'Vänligen mata in ett giltigt momsregistreringsnummer i %s',
            countries: {
                AT: 'Österrike',
                BE: 'Belgien',
                BG: 'Bulgarien',
                BR: 'Brasilien',
                CH: 'Schweiz',
                CY: 'Cypern',
                CZ: 'Tjeckien',
                DE: 'Tyskland',
                DK: 'Danmark',
                EE: 'Estland',
                ES: 'Spanien',
                FI: 'Finland',
                FR: 'Frankrike',
                GB: 'Förenade Kungariket',
                GR: 'Grekland',
                EL: 'Grekland',
                HU: 'Ungern',
                HR: 'Kroatien',
                IE: 'Irland',
                IS: 'Island',
                IT: 'Italien',
                LT: 'Litauen',
                LU: 'Luxemburg',
                LV: 'Lettland',
                MT: 'Malta',
                NL: 'Nederländerna',
                NO: 'Norge',
                PL: 'Polen',
                PT: 'Portugal',
                RO: 'Rumänien',
                RU: 'Ryssland',
                RS: 'Serbien',
                SE: 'Sverige',
                SI: 'Slovenien',
                SK: 'Slovakien',
                VE: 'Venezuela',
                ZA: 'Sydafrika'
            }
        },
        vin: {
            'default': 'Vänligen mata in ett giltigt VIN-nummer'
        },
        zipCode: {
            'default': 'Vänligen mata in ett giltigt postnummer',
            countryNotSupported: 'Landskoden %s stöds inte',
            country: 'Vänligen mata in ett giltigt postnummer i %s',
            countries: {
                AT: 'Österrike',
                BR: 'Brasilien',
                CA: 'Kanada',
                CH: 'Schweiz',
                CZ: 'Tjeckien',
                DE: 'Tyskland',
                DK: 'Danmark',
                FR: 'Frankrike',
                GB: 'Förenade Kungariket',
                IE: 'Irland',
                IT: 'Italien',
                MA: 'Marocko',
                NL: 'Nederländerna',
                PT: 'Portugal',
                RO: 'Rumänien',
                RU: 'Ryssland',
                SE: 'Sverige',
                SG: 'Singapore',
                SK: 'Slovakien',
                US: 'USA'
            }
        }
    });
}(window.jQuery));
