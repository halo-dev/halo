(function($) {
    /**
     * Norwegian language package
     * Translated by @trondulseth
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig base64-kodet verdi'
        },
        between: {
            'default': 'Vennligst fyll ut dette feltet med en verdi mellom %s og %s',
            notInclusive: 'Vennligst tast inn kun en verdi mellom %s og %s'
        },
        callback: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig verdi'
        },
        choice: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig verdi',
            less: 'Vennligst velg minst %s valgmuligheter',
            more: 'Vennligst velg maks %s valgmuligheter',
            between: 'Vennligst velg %s - %s valgmuligheter'
        },
        color: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig'
        },
        creditCard: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig kreditkortnummer'
        },
        cusip: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig CUSIP-nummer'
        },
        cvv: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig CVV-nummer'
        },
        date: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig dato',
            min: 'Vennligst fyll ut dette feltet med en gyldig dato etter %s',
            max: 'Vennligst fyll ut dette feltet med en gyldig dato før %s',
            range: 'Vennligst fyll ut dette feltet med en gyldig dato mellom %s - %s'
        },
        different: {
            'default': 'Vennligst fyll ut dette feltet med en annen verdi'
        },
        digits: {
             'default': 'Vennligst tast inn kun sifre'
        },
        ean: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig EAN-nummer'
        },
        emailAddress: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig epostadresse'
        },
        file: {
            'default': 'Velg vennligst en gyldig fil'
        },
        greaterThan: {
            'default': 'Vennligst fyll ut dette feltet med en verdi større eller lik %s',
            notInclusive: 'Vennligst fyll ut dette feltet med en verdi større enn %s'
        },
        grid: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig GRIDnummer'
        },
        hex: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig hexadecimalt nummer'
        },
        hexColor: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig hexfarge'
        },
        iban: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig IBAN-nummer',
            countryNotSupported: 'Landskoden %s støttes desverre ikke',
            country: 'Vennligst fyll ut dette feltet med et gyldig IBAN-nummer i %s',
            countries: {
                AD: 'Andorra',
                AE: 'De Forente Arabiske Emirater',
                AL: 'Albania',
                AO: 'Angola',
                AT: 'Østerrike',
                AZ: 'Aserbajdsjan',
                BA: 'Bosnia-Hercegovina',
                BE: 'Belgia',
                BF: 'Burkina Faso',
                BG: 'Bulgaria',
                BH: 'Bahrain',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brasil',
                CH: 'Sveits',
                CI: 'Elfenbenskysten',
                CM: 'Kamerun',
                CR: 'Costa Rica',
                CV: 'Kapp Verde',
                CY: 'Kypros',
                CZ: 'Tsjekkia',
                DE: 'Tyskland',
                DK: 'Danmark',
                DO: 'Den dominikanske republikk',
                DZ: 'Algerie',
                EE: 'Estland',
                ES: 'Spania',
                FI: 'Finland',
                FO: 'Færøyene',
                FR: 'Frankrike',
                GB: 'Storbritannia',
                GE: 'Georgia',
                GI: 'Gibraltar',
                GL: 'Grønland',
                GR: 'Hellas',
                GT: 'Guatemala',
                HR: 'Kroatia',
                HU: 'Ungarn',
                IE: 'Irland',
                IL: 'Israel',
                IR: 'Iran',
                IS: 'Island',
                IT: 'Italia',
                JO: 'Jordan',
                KW: 'Kuwait',
                KZ: 'Kasakhstan',
                LB: 'Libanon',
                LI: 'Liechtenstein',
                LT: 'Litauen',
                LU: 'Luxembourg',
                LV: 'Latvia',
                MC: 'Monaco',
                MD: 'Moldova',
                ME: 'Montenegro',
                MG: 'Madagaskar',
                MK: 'Makedonia',
                ML: 'Mali',
                MR: 'Mauritania',
                MT: 'Malta',
                MU: 'Mauritius',
                MZ: 'Mosambik',
                NL: 'Nederland',
                NO: 'Norge',
                PK: 'Pakistan',
                PL: 'Polen',
                PS: 'Palestina',
                PT: 'Portugal',
                QA: 'Qatar',
                RO: 'Romania',
                RS: 'Serbia',
                SA: 'Saudi-Arabia',
                SE: 'Sverige',
                SI: 'Slovenia',
                SK: 'Slovakia',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunisia',
                TR: 'Tyrkia',
                VG: 'De Britiske Jomfruøyene'
            }
        },
        id: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig identifikasjons-nummer',
            countryNotSupported: 'Landskoden %s støttes desverre ikke',
            country: 'Vennligst fyll ut dette feltet med et gyldig identifikasjons-nummer i %s',
            countries: {
                BA: 'Bosnien-Hercegovina',
                BG: 'Bulgaria',
                BR: 'Brasil',
                CH: 'Sveits',
                CL: 'Chile',
                CN: 'Kina',
                CZ: 'Tsjekkia',
                DK: 'Danmark',
                EE: 'Estland',
                ES: 'Spania',
                FI: 'Finland',
                HR: 'Kroatia',
                IE: 'Irland',
                IS: 'Island',
                LT: 'Litauen',
                LV: 'Latvia',
                ME: 'Montenegro',
                MK: 'Makedonia',
                NL: 'Nederland',
                RO: 'Romania',
                RS: 'Serbia',
                SE: 'Sverige',
                SI: 'Slovenia',
                SK: 'Slovakia',
                SM: 'San Marino',
                TH: 'Thailand',
                ZA: 'Sør-Afrika'
            }
        },
        identical: {
            'default': 'Vennligst fyll ut dette feltet med den samme verdi'
        },
        imei: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig IMEI-nummer'
        },
        imo: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig IMO-nummer'
        },
        integer: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig tall'
        },
        ip: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig IP adresse',
            ipv4: 'Vennligst fyll ut dette feltet med en gyldig IPv4 adresse',
            ipv6: 'Vennligst fyll ut dette feltet med en gyldig IPv6 adresse'
        },
        isbn: {
            'default': 'Vennligst fyll ut dette feltet med ett gyldig ISBN-nummer'
        },
        isin: {
            'default': 'Vennligst fyll ut dette feltet med ett gyldig ISIN-nummer'
        },
        ismn: {
            'default': 'Vennligst fyll ut dette feltet med ett gyldig ISMN-nummer'
        },
        issn: {
            'default': 'Vennligst fyll ut dette feltet med ett gyldig ISSN-nummer'
        },
        lessThan: {
            'default': 'Vennligst fyll ut dette feltet med en verdi mindre eller lik %s',
            notInclusive: 'Vennligst fyll ut dette feltet med en verdi mindre enn %s'
        },
        mac: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig MAC adresse'
        },
        meid: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig MEID-nummer'
        },
        notEmpty: {
            'default': 'Vennligst fyll ut dette feltet'
        },
        numeric: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig flydende desimaltal'
        },
        phone: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig telefonnummer',
            countryNotSupported: 'Landskoden %s støttes desverre ikke',
            country: 'Vennligst fyll ut dette feltet med et gyldig telefonnummer i %s',
            countries: {
                BR: 'Brasil',
                CN: 'Kina',
                CZ: 'Tsjekkia',
                DE: 'Tyskland',
                DK: 'Danmark',
                ES: 'Spania',
                FR: 'Frankrike',
                GB: 'Storbritannia',
                MA: 'Marokko',
                PK: 'Pakistan',
                RO: 'Rumenia',
                RU: 'Russland',
                SK: 'Slovakia',
                TH: 'Thailand',
                US: 'USA',
                VE: 'Venezuela'
            }
        },
        regexp: {
            'default': 'Vennligst fyll ut dette feltet med en verdi som matcher mønsteret'
        },
        remote: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig verdi'
        },
        rtn: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig RTN-nummer'
        },
        sedol: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig SEDOL-nummer'
        },
        siren: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig SIREN-nummer'
        },
        siret: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig SIRET-nummer'
        },
        step: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig trin af %s'
        },
        stringCase: {
            'default': 'Venligst fyll inn dette feltet kun med små bokstaver',
            upper: 'Venligst fyll inn dette feltet kun med store bokstaver'
        },
        stringLength: {
            'default': 'Vennligst fyll ut dette feltet med en verdi af gyldig længde',
            less: 'Vennligst fyll ut dette feltet med mindre enn %s tegn',
            more: 'Vennligst fyll ut dette feltet med mer enn %s tegn',
            between: 'Vennligst fyll ut dette feltet med en verdi mellom %s og %s tegn'
        },
        uri: {
            'default': 'Vennligst fyll ut dette feltet med en gyldig URI'
        },
        uuid: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig UUID-nummer',
            version: 'Vennligst fyll ut dette feltet med en gyldig UUID version %s-nummer'
        },
        vat: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig MVA nummer',
            countryNotSupported: 'Landskoden %s støttes desverre ikke',
            country: 'Vennligst fyll ut dette feltet med et gyldig MVA nummer i %s',
            countries: {
                AT: 'Østerrike',
                BE: 'Belgia',
                BG: 'Bulgaria',
                BR: 'Brasil',
                CH: 'Schweiz',
                CY: 'Cypern',
                CZ: 'Tsjekkia',
                DE: 'Tyskland',
                DK: 'Danmark',
                EE: 'Estland',
                ES: 'Spania',
                FI: 'Finland',
                FR: 'Frankrike',
                GB: 'Storbritania',
                GR: 'Hellas',
                EL: 'Hellas',
                HU: 'Ungarn',
                HR: 'Kroatia',
                IE: 'Irland',
                IS: 'Island',
                IT: 'Italia',
                LT: 'Litauen',
                LU: 'Luxembourg',
                LV: 'Latvia',
                MT: 'Malta',
                NL: 'Nederland',
                NO: 'Norge',
                PL: 'Polen',
                PT: 'Portugal',
                RO: 'Romania',
                RU: 'Russland',
                RS: 'Serbia',
                SE: 'Sverige',
                SI: 'Slovenia',
                SK: 'Slovakia',
                VE: 'Venezuela',
                ZA: 'Sør-Afrika'
            }
        },
        vin: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig VIN-nummer'
        },
        zipCode: {
            'default': 'Vennligst fyll ut dette feltet med et gyldig postnummer',
            countryNotSupported: 'Landskoden %s støttes desverre ikke',
            country: 'Vennligst fyll ut dette feltet med et gyldig postnummer i %s',
            countries: {
                AT: 'Østerrike',
                BR: 'Brasil',
                CA: 'Canada',
                CH: 'Schweiz',
                CZ: 'Tsjekkia',
                DE: 'Tyskland',
                DK: 'Danmark',
                FR: 'Frankrike',
                GB: 'Storbritannia',
                IE: 'Irland',
                IT: 'Italia',
                MA: 'Marokko',
                NL: 'Nederland',
                PT: 'Portugal',
                RO: 'Romania',
                RU: 'Russland',
                SE: 'Sverige',
                SG: 'Singapore',
                SK: 'Slovakia',
                US: 'USA'
            }
        }
    });
}(window.jQuery));
