(function($) {
    /**
     * Albanian language package
     * Translated by @desaretiuss
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Ju lutem përdorni sistemin e kodimit Base64'
        },
        between: {
            'default': 'Ju lutem vendosni një vlerë midis %s dhe %s',
            notInclusive: 'Ju lutem vendosni një vlerë rreptësisht midis %s dhe %s'
        },
        callback: {
            'default': 'Ju lutem vendosni një vlerë të vlefshme'
        },
        choice: {
            'default': 'Ju lutem vendosni një vlerë të vlefshme',
            less: 'Ju lutem përzgjidhni së paku %s mundësi',
            more: 'Ju lutem përzgjidhni së shumti %s mundësi ',
            between: 'Ju lutem përzgjidhni %s - %s mundësi'
        },
        color: {
            'default': 'Ju lutem vendosni një ngjyrë të vlefshme'
        },
        creditCard: {   
            'default': 'Ju lutem vendosni një numër karte krediti të vlefshëm'
        },
        cusip: {
            'default': 'Ju lutem vendosni një numër CUSIP të vlefshëm'
        },
        cvv: {
            'default': 'Ju lutem vendosni një numër CVV të vlefshëm'
        },
        date: {
            'default': 'Ju lutem vendosni një datë të saktë',
            min: 'Ju lutem vendosni një datë pas %s',
            max: 'Ju lutem vendosni një datë para %s',
            range: 'Ju lutem vendosni një datë midis %s - %s'
        },
        different: {
            'default': 'Ju lutem vendosni një vlerë tjetër'
        },
        digits: {
             'default': 'Ju lutem vendosni vetëm numra'
        },
        ean: {
            'default': 'Ju lutem vendosni një numër EAN të vlefshëm'
        },
        emailAddress: {
            'default': 'Ju lutem vendosni një adresë email të vlefshme'
        },
        file: {
            'default': 'Ju lutem përzgjidhni një skedar të vlefshëm'
        },
        greaterThan: {
            'default': 'Ju lutem vendosni një vlerë më të madhe ose të barabartë me %s',
            notInclusive: 'Ju lutem vendosni një vlerë më të madhe se %s'
        },
        grid: {
            'default': 'Ju lutem vendosni një numër GRId të vlefshëm'
        },
        hex: {
            'default': 'Ju lutem vendosni një numër të saktë heksadecimal'
        },
        hexColor: {
            'default': 'Ju lutem vendosni një ngjyrë të vlefshme heksadecimale'
        },
        iban: {
            'default': 'Ju lutem vendosni një numër IBAN të vlefshëm',
            countryNotSupported: 'Kodi i shtetit %s nuk është i mundësuar',
            country: 'Ju lutem vendosni një numër IBAN të vlefshëm në %s',
            countries: {
                AD: 'Andora',
                AE: 'Emiratet e Bashkuara Arabe',
                AL: 'Shqipëri',
                AO: 'Angola',
                AT: 'Austri',
                AZ: 'Azerbajxhan',
                BA: 'Bosnjë dhe Hercegovinë',
                BE: 'Belgjikë',
                BF: 'Burkina Faso',
                BG: 'Bullgari',
                BH: 'Bahrein',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brazil',
                CH: 'Zvicër',
                CI: 'Bregu i fildishtë',
                CM: 'Kamerun',
                CR: 'Kosta Rika',
                CV: 'Kepi i Gjelbër',
                CY: 'Qipro',
                CZ: 'Republika Çeke',
                DE: 'Gjermani',
                DK: 'Danimarkë',
                DO: 'Dominika',
                DZ: 'Algjeri',
                EE: 'Estoni',
                ES: 'Spanjë',
                FI: 'Finlandë',
                FO: 'Ishujt Faroe',
                FR: 'Francë',
                GB: 'Mbretëria e Bashkuar',
                GE: 'Gjeorgji',
                GI: 'Gjibraltar',
                GL: 'Groenlandë',
                GR: 'Greqi',
                GT: 'Guatemalë',
                HR: 'Kroaci',
                HU: 'Hungari',
                IE: 'Irlandë',
                IL: 'Izrael',
                IR: 'Iran',
                IS: 'Islandë',
                IT: 'Itali',
                JO: 'Jordani',
                KW: 'Kuvajt',
                KZ: 'Kazakistan',
                LB: 'Liban',
                LI: 'Lihtenshtejn',
                LT: 'Lituani',
                LU: 'Luksemburg',
                LV: 'Letoni',
                MC: 'Monako',
                MD: 'Moldavi',
                ME: 'Mal i Zi',
                MG: 'Madagaskar',
                MK: 'Maqedoni',
                ML: 'Mali',
                MR: 'Mauritani',
                MT: 'Maltë',
                MU: 'Mauricius',
                MZ: 'Mozambik',
                NL: 'Hollandë',
                NO: 'Norvegji',
                PK: 'Pakistan',
                PL: 'Poloni',
                PS: 'Palestinë',
                PT: 'Portugali',
                QA: 'Katar',
                RO: 'Rumani',
                RS: 'Serbi',
                SA: 'Arabi Saudite',
                SE: 'Suedi',
                SI: 'Slloveni',
                SK: 'Sllovaki',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunizi',
                TR: 'Turqi',
                VG: 'Ishujt Virxhin Britanikë'
            }
        },
        id: {
            'default': 'Ju lutem vendosni një numër identifikimi të vlefshëm ',
            countryNotSupported: 'Kodi i shtetit %s nuk është i mundësuar',
            country: 'Ju lutem vendosni një numër identifikimi të vlefshëm në %s',
            countries: {
                BA: 'Bosnjë dhe Hercegovinë',
                BG: 'Bullgari',
                BR: 'Brazil',
                CH: 'Zvicër',
                CL: 'Kili',
                CN: 'Kinë',
                CZ: 'Republika Çeke',
                DK: 'Danimarkë',
                EE: 'Estoni',
                ES: 'Spanjë',
                FI: 'Finlandë',
                HR: 'Kroaci',
                IE: 'Irlandë',
                IS: 'Islandë',
                LT: 'Lituani',
                LV: 'Letoni',
                ME: 'Mal i Zi',
                MK: 'Maqedoni',
                NL: 'Hollandë',
                RO: 'Rumani',
                RS: 'Serbi',
                SE: 'Suedi',
                SI: 'Slloveni',
                SK: 'Slovaki',
                SM: 'San Marino',
                TH: 'Tajlandë',
                ZA: 'Afrikë e Jugut'
            }
        },
        identical: {
            'default': 'Ju lutem vendosni të njëjtën vlerë'
        },
        imei: {
            'default': 'Ju lutem vendosni numër IMEI të njëjtë'
        },
        imo: {
            'default': 'Ju lutem vendosni numër IMO të vlefshëm'
        },
        integer: {
            'default': 'Ju lutem vendosni një numër të vlefshëm'
        },
        ip: {
            'default': 'Ju lutem vendosni një adresë IP të vlefshme',
            ipv4: 'Ju lutem vendosni një adresë IPv4 të vlefshme',
            ipv6: 'Ju lutem vendosni një adresë IPv6 të vlefshme'
        },
        isbn: {
            'default': 'Ju lutem vendosni një numër ISBN të vlefshëm'
        },
        isin: {
            'default': 'Ju lutem vendosni një numër ISIN të vlefshëm'
        },
        ismn: {
            'default': 'Ju lutem vendosni një numër ISMN të vlefshëm'
        },
        issn: {
            'default': 'Ju lutem vendosni një numër ISSN të vlefshëm'
        },
        lessThan: {
            'default': 'Ju lutem vendosni një vlerë më të madhe ose të barabartë me %s',
            notInclusive: 'Ju lutem vendosni një vlerë më të vogël se %s'
        },
        mac: {
            'default': 'Ju lutem vendosni një adresë MAC të vlefshme'
        },
        meid: {
            'default': 'Ju lutem vendosni një numër MEID të vlefshëm'
        },
        notEmpty: {
            'default': 'Ju lutem vendosni një vlerë'
        },
        numeric: {
            'default': 'Ju lutem vendosni një numër me presje notuese të saktë'
        },
        phone: {
            'default': 'Ju lutem vendosni një numër telefoni të vlefshëm',
            countryNotSupported: 'Kodi i shtetit %s nuk është i mundësuar',
            country: 'Ju lutem vendosni një numër telefoni të vlefshëm në %s',
            countries: {
                BR: 'Brazil',
                CN: 'Kinë',
                CZ: 'Republika Çeke',
                DE: 'Gjermani',
                DK: 'Danimarkë',
                ES: 'Spanjë',
                FR: 'Francë',
                GB: 'Mbretëria e Bashkuar',
                MA: 'Marok',
                PK: 'Pakistan',
                RO: 'Rumani',
                RU: 'Rusi',
                SK: 'Sllovaki',
                TH: 'Tajlandë',
                US: 'SHBA',
                VE: 'Venezuelë'
            }
        },
        regexp: {
            'default': 'Ju lutem vendosni një vlerë që përputhet me modelin'
        },
        remote: {
            'default': 'Ju lutem vendosni një vlerë të vlefshme'
        },
        rtn: {
            'default': 'Ju lutem vendosni një numër RTN të vlefshëm'
        },
        sedol: {
            'default': 'Ju lutem vendosni një numër SEDOL të vlefshëm'
        },
        siren: {
            'default': 'Ju lutem vendosni një numër SIREN të vlefshëm'
        },
        siret: {
            'default': 'Ju lutem vendosni një numër SIRET të vlefshëm'
        },
        step: {
            'default': 'Ju lutem vendosni një hap të vlefshëm të %s'
        },
        stringCase: {
            'default': 'Ju lutem përdorni vetëm shenja të vogla të shtypit',
            upper: 'Ju lutem përdorni vetëm shenja të mëdha të shtypit'
        },
        stringLength: {
            'default': 'Ju lutem vendosni një vlerë me gjatësinë e duhur',
            less: 'Ju lutem vendosni më pak se %s simbole',
            more: 'Ju lutem vendosni më shumë se %s simbole',
            between: 'Ju lutem vendosni një vlerë me gjatësi midis %s dhe %s simbole'
        },
        uri: {
            'default': 'Ju lutem vendosni një URI të vlefshme'
        },
        uuid: {
            'default': 'Ju lutem vendosni një numër UUID të vlefshëm',
            version:   'Ju lutem vendosni një numër UUID version %s të vlefshëm'
        },
        vat: {
            'default': 'Ju lutem vendosni një numër VAT të vlefshëm',
            countryNotSupported: 'Kodi i shtetit %s nuk është i mundësuar',
            country: 'Ju lutem vendosni një numër VAT të vlefshëm në %s',
            countries: {
                AT: 'Austri',
                BE: 'Belgjikë',
                BG: 'Bullgari',
                BR: 'Brazil',
                CH: 'Zvicër',
                CY: 'Qipro',
                CZ: 'Republika Çeke',
                DE: 'Gjermani',
                DK: 'Danimarkë',
                EE: 'Estoni',
                ES: 'Spanjë',
                FI: 'Finlandë',
                FR: 'Francë',
                GB: 'Mbretëria e Bashkuar',
                GR: 'Greqi',
                EL: 'Greqi',
                HU: 'Hungari',
                HR: 'Kroaci',
                IE: 'Irlandë',
                IS: 'Iclandë',
                IT: 'Itali',
                LT: 'Lituani',
                LU: 'Luksemburg',
                LV: 'Letoni',
                MT: 'Maltë',
                NL: 'Hollandë',
                NO: 'Norvegji',
                PL: 'Poloni',
                PT: 'Portugali',
                RO: 'Rumani',
                RU: 'Rusi',
                RS: 'Serbi',
                SE: 'Suedi',
                SI: 'Slloveni',
                SK: 'Sllovaki',
                VE: 'Venezuelë',
                ZA: 'Afrikë e Jugut'
            }
        },
        vin: {
            'default': 'Ju lutem vendosni një numër VIN të vlefshëm'
        },
        zipCode: {
            'default': 'Ju lutem vendosni një kod postar të vlefshëm',
            countryNotSupported: 'Kodi i shtetit %s nuk është i mundësuar',
            country: 'Ju lutem vendosni një kod postar të vlefshëm në %s',
            countries: {
                AT: 'Austri',
                BR: 'Brazil',
                CA: 'Kanada',
                CH: 'Zvicër',
                CZ: 'Republika Çeke',
                DE: 'Gjermani',
                DK: 'Danimarkë',
                FR: 'Francë',
                GB: 'Mbretëria e Bashkuar',
                IE: 'Irlandë',
                IT: 'Itali',
                MA: 'Marok',
                NL: 'Hollandë',
                PT: 'Portugali',
                RO: 'Rumani',
                RU: 'Rusi',
                SE: 'Suedi',
                SG: 'Singapor',
                SK: 'Sllovaki',
                US: 'SHBA'
            }
        }
    });
}(window.jQuery));
