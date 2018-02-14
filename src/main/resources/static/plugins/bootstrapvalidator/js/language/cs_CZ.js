(function($) {
    /**
     * Czech language package
     * Translated by @AdwinTrave. Improved by @cuchac
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Prosím zadejte správný base64'
        },
        between: {
            'default': 'Prosím zadejte hodnotu mezi %s a %s',
            notInclusive: 'Prosím zadejte hodnotu mezi %s a %s (včetně těchto čísel)'
        },
        callback: {
            'default': 'Prosím zadejte správnou hodnotu'
        },
        choice: {
            'default': 'Prosím vyberte správnou hodnotu',
            less: 'Hodnota musí být minimálně %s',
            more: 'Hodnota nesmí být více jak %s',
            between: 'Prosím vyberte mezi %s a %s'
        },
        color: {
            'default': 'Prosím zadejte správnou barvu'
        },
        creditCard: {
            'default': 'Prosím zadejte správné číslo kreditní karty'
        },
        cusip: {
            'default': 'Prosím zadejte správné CUSIP číslo'
        },
        cvv: {
            'default': 'Prosím zadejte správné CVV číslo'
        },
        date: {
            'default': 'Prosím zadejte správné datum',
            min: 'Prosím zadejte datum před %s',
            max: 'Prosím zadejte datum po %s',
            range: 'Prosím zadejte datum v rozmezí %s až %s'
        },
        different: {
            'default': 'Prosím zadejte jinou hodnotu'
        },
        digits: {
             'default': 'Toto pole může obsahovat pouze čísla'
        },
        ean: {
            'default': 'Prosím zadejte správné EAN číslo'
        },
        emailAddress: {
            'default': 'Prosím zadejte správnou emailovou adresu'
        },
        file: {
            'default': 'Prosím vyberte soubor'
        },
        greaterThan: {
            'default': 'Prosím zadejte hodnotu větší nebo rovnu %s',
            notInclusive: 'Prosím zadejte hodnotu větší než %s'
        },
        grid: {
            'default': 'Prosím zadejte správné GRId číslo'
        },
        hex: {
            'default': 'Prosím zadejte správné hexadecimální číslo'
        },
        hexColor: {
            'default': 'Prosím zadejte správnou hex barvu'
        },
        iban: {
            'default': 'Prosím zadejte správné IBAN číslo',
            countryNotSupported: 'IBAN pro %s není podporován',
            country: 'Prosím zadejte správné IBAN číslo pro %s',
            countries: {
                AD: 'Andorru',
                AE: 'Spojené arabské emiráty',
                AL: 'Albanii',
                AO: 'Angolu',
                AT: 'Rakousko',
                AZ: 'Ázerbajdžán',
                BA: 'Bosnu a Herzegovinu',
                BE: 'Belgie',
                BF: 'Burkina Faso',
                BG: 'Bulharsko',
                BH: 'Bahrajn',
                BI: 'Burundi',
                BJ: 'Benin',
                BR: 'Brazílii',
                CH: 'Švýcarsko',
                CI: 'Pobřeží slonoviny',
                CM: 'Kamerun',
                CR: 'Kostariku',
                CV: 'Cape Verde',
                CY: 'Kypr',
                CZ: 'Českou republiku',
                DE: 'Německo',
                DK: 'Dánsko',
                DO: 'Dominikánskou republiku',
                DZ: 'Alžírsko',
                EE: 'Estonsko',
                ES: 'Španělsko',
                FI: 'Finsko',
                FO: 'Faerské ostrovy',
                FR: 'Francie',
                GB: 'Velkou Británii',
                GE: 'Gruzii',
                GI: 'Gibraltar',
                GL: 'Grónsko',
                GR: 'Řecko',
                GT: 'Guatemala',
                HR: 'Chorvatsko',
                HU: 'Maďarsko',
                IE: 'Irsko',
                IL: 'Israel',
                IR: 'Irán',
                IS: 'Island',
                IT: 'Itálii',
                JO: 'Jordansko',
                KW: 'Kuwait',
                KZ: 'Kazakhstán',
                LB: 'Lebanon',
                LI: 'Lichtenštejnsko',
                LT: 'Litvu',
                LU: 'Lucembursko',
                LV: 'Lotyšsko',
                MC: 'Monaco',
                MD: 'Moldavsko',
                ME: 'Černou Horu',
                MG: 'Madagaskar',
                MK: 'Makedonii',
                ML: 'Mali',
                MR: 'Mauritánii',
                MT: 'Malta',
                MU: 'Mauritius',
                MZ: 'Mosambik',
                NL: 'Nizozemsko',
                NO: 'Norsko',
                PK: 'Pakistán',
                PL: 'Polsko',
                PS: 'Palestinu',
                PT: 'Portugalsko',
                QA: 'Katar',
                RO: 'Rumunsko',
                RS: 'Srbsko',
                SA: 'Saudskou Arábii',
                SE: 'Švédsko',
                SI: 'Slovinsko',
                SK: 'Slovensko',
                SM: 'San Marino',
                SN: 'Senegal',
                TN: 'Tunisko',
                TR: 'Turecko',
                VG: 'Britské Panenské ostrovy'
            }
        },
        id: {
            'default': 'Prosím zadejte správné rodné číslo',
            countryNotSupported: 'Rodné číslo pro %s není podporované',
            country: 'Prosím zadejte správné rodné číslo pro %s',
            countries: {
                BA: 'Bosnu a Hercegovinu',
                BG: 'Bulharsko',
                BR: 'Brazílii',
                CH: 'Švýcarsko',
                CL: 'Chile',
                CN: 'Čína',
                CZ: 'Českou Republiku',
                DK: 'Dánsko',
                EE: 'Estonsko',
                ES: 'Špaňelsko',
                FI: 'Finsko',
                HR: 'Chorvatsko',
                IE: 'Irsko',
                IS: 'Island',
                LT: 'Litvu',
                LV: 'Lotyšsko',
                ME: 'Montenegro',
                MK: 'Makedonii',
                NL: 'Nizozemí',
                RO: 'Rumunsko',
                RS: 'Srbsko',
                SE: 'Švédsko',
                SI: 'Slovinsko',
                SK: 'Slovensko',
                SM: 'San Marino',
                TH: 'Thajsko',
                ZA: 'Jižní Afriku'
            }
        },
        identical: {
            'default': 'Prosím zadejte stejnou hodnotu'
        },
        imei: {
            'default': 'Prosím zadejte správné IMEI číslo'
        },
        imo: {
            'default': 'Prosím zadejte správné IMO číslo'
        },
        integer: {
            'default': 'Prosím zadejte celé číslo'
        },
        ip: {
            'default': 'Prosím zadejte správnou IP adresu',
            ipv4: 'Prosím zadejte správnou IPv4 adresu',
            ipv6: 'Prosím zadejte správnou IPv6 adresu'
        },
        isbn: {
            'default': 'Prosím zadejte správné ISBN číslo'
        },
        isin: {
            'default': 'Prosím zadejte správné ISIN číslo'
        },
        ismn: {
            'default': 'Prosím zadejte správné ISMN číslo'
        },
        issn: {
            'default': 'Prosím zadejte správné ISSN číslo'
        },
        lessThan: {
            'default': 'Prosím zadejte hodnotu menší nebo rovno %s',
            notInclusive: 'Prosím zadejte hodnotu menčí než %s'
        },
        mac: {
            'default': 'Prosím zadejte správnou MAC adresu'
        },
        meid: {
            'default': 'Prosím zadejte správné MEID číslo'
        },
        notEmpty: {
            'default': 'Toto pole nesmí být prázdné'
        },
        numeric: {
            'default': 'Prosím zadejte číselnou hodnotu'
        },
        phone: {
            'default': 'Prosím zadejte správné telefoní číslo',
            countryNotSupported: 'Telefoní číslo pro %s není podporované',
            country: 'Prosím zadejte správné telefoní číslo pro %s',
            countries: {
                BR: 'Brazílii',
                CN: 'Čína',
                CZ: 'Českou Republiku',
                DE: 'Německo',
                DK: 'Dánsko',
                ES: 'Španělsko',
                FR: 'Francie',
                GB: 'Velkou Británii',
                MA: 'Maroko',
                PK: 'Pákistán',
                RO: 'Rumunsko',
                RU: 'Rusko',
                SK: 'Slovensko',
                TH: 'Thajsko',
                US: 'Spojené Státy Americké',
                VE: 'Venezuelský'
            }
        },
        regexp: {
            'default': 'Prosím zadejte hodnotu splňující zadání'
        },
        remote: {
            'default': 'Prosím zadejte správnou hodnotu'
        },
        rtn: {
            'default': 'Prosím zadejte správné RTN číslo'
        },
        sedol: {
            'default': 'Prosím zadejte správné SEDOL číslo'
        },
        siren: {
            'default': 'Prosím zadejte správné SIREN číslo'
        },
        siret: {
            'default': 'Prosím zadejte správné SIRET číslo'
        },
        step: {
            'default': 'Prosím zadejte správný krok %s'
        },
        stringCase: {
            'default': 'Pouze malá písmen jsou povoleny v tomto poli',
            upper: 'Pouze velké písmena jsou povoleny v tomto poli'
        },
        stringLength: {
            'default': 'Toto pole nesmí být prázdné',
            less: 'Prosím zadejte méně než %s znaků',
            more: 'Prosím zadejte více než %s znaků',
            between: 'Prosím zadejte mezi %s a %s znaky'
        },
        uri: {
            'default': 'Prosím zadejte správnou URI'
        },
        uuid: {
            'default': 'Prosím zadejte správné UUID číslo',
            version: 'Prosím zadejte správné UUID verze %s'
        },
        vat: {
            'default': 'Prosím zadejte správné VAT číslo',
            countryNotSupported: 'VAT pro %s není podporované',
            country: 'Prosím zadejte správné VAT číslo pro %s',
            countries: {
                AT: 'Rakousko',
                BE: 'Belgii',
                BG: 'Bulharsko',
                BR: 'Brazílii',
                CH: 'Švýcarsko',
                CY: 'Kypr',
                CZ: 'Českou Republiku',
                DE: 'Německo',
                DK: 'Dánsko',
                EE: 'Estonsko',
                ES: 'Špaňelsko',
                FI: 'Finsko',
                FR: 'Francie',
                GB: 'Velkou Británii',
                GR: 'Řecko',
                EL: 'Řecko',
                HU: 'Maďarsko',
                HR: 'Chorvatsko',
                IE: 'Irsko',
                IS: 'Island',
                IT: 'Itálie',
                LT: 'Litvu',
                LU: 'Lucembursko',
                LV: 'Lotyšsko',
                MT: 'Maltu',
                NL: 'Nizozemí',
                NO: 'Norsko',
                PL: 'Polsko',
                PT: 'Portugalsko',
                RO: 'Rumunsko',
                RU: 'Rusko',
                RS: 'Srbsko',
                SE: 'Švédsko',
                SI: 'Slovinsko',
                SK: 'Slovensko',
                VE: 'Venezuelský',
                ZA: 'Jižní Afriku'
            }
        },
        vin: {
            'default': 'Prosím zadejte správné VIN číslo'
        },
        zipCode: {
            'default': 'Prosím zadejte správné PSČ',
            countryNotSupported: '%s není podporované',
            country: 'Prosím zadejte správné PSČ pro %s',
            countries: {
                AT: 'Rakousko',
                BR: 'Brazílie',
                CA: 'Kanada',
                CH: 'Švýcarsko',
                CZ: 'Českou Republiku',
                DE: 'Německo',
                DK: 'Dánsko',
                FR: 'Francie',
                GB: 'Velkou Británii',
                IE: 'Irsko',
                IT: 'Itálie',
                MA: 'Maroko',
                NL: 'Nizozemí',
                PT: 'Portugalsko',
                RO: 'Rumunsko',
                RU: 'Rusko',
                SE: 'Švédsko',
                SG: 'Singapur',
                SK: 'Slovensko',
                US: 'Spojené Státy Americké'
            }
        }
    });
}(window.jQuery));
