(function($) {
    /**
     * French language package
     * Translated by @dlucazeau. Updated by @neilime, @jazzzz
     */
    $.fn.bootstrapValidator.i18n = $.extend(true, $.fn.bootstrapValidator.i18n, {
        base64: {
            'default': 'Veuillez fournir une donnée correctement encodée en Base64'
        },
        between: {
            'default': 'Veuillez fournir une valeur comprise entre %s et %s',
            notInclusive: 'Veuillez fournir une valeur strictement comprise entre %s et %s'
        },
        callback: {
            'default': 'Veuillez fournir une valeur valide'
        },
        choice: {
            'default': 'Veuillez fournir une valeur valide',
            less: 'Veuillez choisir au minimum %s options',
            more: 'Veuillez choisir au maximum %s options',
            between: 'Veuillez choisir de %s à %s options'
        },
        color: {
            'default': 'Veuillez fournir une couleur valide'
        },
        creditCard: {
            'default': 'Veuillez fournir un numéro de carte de crédit valide'
        },
        cusip: {
            'default': 'Veuillez fournir un code CUSIP valide'
        },
        cvv: {
            'default': 'Veuillez fournir un code CVV valide'
        },
        date: {
            'default': 'Veuillez fournir une date valide',
            'min': 'Veuillez fournir une date supérieure à %s',
            'max': 'Veuillez fournir une date inférieure à %s',
            'range': 'Veuillez fournir une date comprise entre %s et %s'
        },
        different: {
            'default': 'Veuillez fournir une valeur différente'
        },
        digits: {
            'default': 'Veuillez ne fournir que des chiffres'
        },
        ean: {
            'default': 'Veuillez fournir un code-barre EAN valide'
        },
        emailAddress: {
            'default': 'Veuillez fournir une adresse e-mail valide'
        },
        file: {
            'default': 'Veuillez choisir un fichier valide'
        },
        greaterThan: {
            'default': 'Veuillez fournir une valeur supérieure ou égale à %s',
            notInclusive: 'Veuillez fournir une valeur supérieure à %s'
        },
        grid: {
            'default': 'Veuillez fournir un code GRId valide'
        },
        hex: {
            'default': 'Veuillez fournir un nombre hexadécimal valide'
        },
        hexColor: {
            'default': 'Veuillez fournir une couleur hexadécimale valide'
        },
        iban: {
            'default': 'Veuillez fournir un code IBAN valide',
            countryNotSupported: 'Le code de pays %s n\'est pas accepté',
            country: 'Veuillez fournir un code IBAN valide pour %s',
            countries: {
                AD: 'Andorre',
                AE: 'Émirats Arabes Unis',
                AL: 'Albanie',
                AO: 'Angola',
                AT: 'Autriche',
                AZ: 'Azerbaïdjan',
                BA: 'Bosnie-Herzégovine',
                BE: 'Belgique',
                BF: 'Burkina Faso',
                BG: 'Bulgarie',
                BH: 'Bahrein',
                BI: 'Burundi',
                BJ: 'Bénin',
                BR: 'Brésil',
                CH: 'Suisse',
                CI: 'Côte d\'ivoire',
                CM: 'Cameroun',
                CR: 'Costa Rica',
                CV: 'Cap Vert',
                CY: 'Chypre',
                CZ: 'République Tchèque',
                DE: 'Allemagne',
                DK: 'Danemark',
                DO: 'République Dominicaine',
                DZ: 'Algérie',
                EE: 'Estonie',
                ES: 'Espagne',
                FI: 'Finlande',
                FO: 'Îles Féroé',
                FR: 'France',
                GB: 'Royaume Uni',
                GE: 'Géorgie',
                GI: 'Gibraltar',
                GL: 'Groënland',
                GR: 'Gréce',
                GT: 'Guatemala',
                HR: 'Croatie',
                HU: 'Hongrie',
                IE: 'Irlande',
                IL: 'Israël',
                IR: 'Iran',
                IS: 'Islande',
                IT: 'Italie',
                JO: 'Jordanie',
                KW: 'Koweït',
                KZ: 'Kazakhstan',
                LB: 'Liban',
                LI: 'Liechtenstein',
                LT: 'Lithuanie',
                LU: 'Luxembourg',
                LV: 'Lettonie',
                MC: 'Monaco',
                MD: 'Moldavie',
                ME: 'Monténégro',
                MG: 'Madagascar',
                MK: 'Macédoine',
                ML: 'Mali',
                MR: 'Mauritanie',
                MT: 'Malte',
                MU: 'Maurice',
                MZ: 'Mozambique',
                NL: 'Pays-Bas',
                NO: 'Norvège',
                PK: 'Pakistan',
                PL: 'Pologne',
                PS: 'Palestine',
                PT: 'Portugal',
                QA: 'Quatar',
                RO: 'Roumanie',
                RS: 'Serbie',
                SA: 'Arabie Saoudite',
                SE: 'Suède',
                SI: 'Slovènie',
                SK: 'Slovaquie',
                SM: 'Saint-Marin',
                SN: 'Sénégal',
                TN: 'Tunisie',
                TR: 'Turquie',
                VG: 'Îles Vierges britanniques'
            }
        },
        id: {
            'default': 'Veuillez fournir un numéro d\'identification valide',
            countryNotSupported: 'Le code de pays %s n\'est pas accepté',
            country: 'Veuillez fournir un numéro d\'identification valide pour %s',
            countries: {
                BA: 'Bosnie-Herzégovine',
                BG: 'Bulgarie',
                BR: 'Brésil',
                CH: 'Suisse',
                CL: 'Chili',
                CN: 'Chine',
                CZ: 'République Tchèque',
                DK: 'Danemark',
                EE: 'Estonie',
                ES: 'Espagne',
                FI: 'Finlande',
                HR: 'Croatie',
                IE: 'Irlande',
                IS: 'Islande',
                LT: 'Lituanie',
                LV: 'Lettonie',
                ME: 'Monténégro',
                MK: 'Macédoine',
                NL: 'Pays-Bas',
                RO: 'Roumanie',
                RS: 'Serbie',
                SE: 'Suède',
                SI: 'Slovénie',
                SK: 'Slovaquie',
                SM: 'Saint-Marin',
                TH: 'Thaïlande',
                ZA: 'Afrique du Sud'
            }
        },
        identical: {
            'default': 'Veuillez fournir la même valeur'
        },
        imei: {
            'default': 'Veuillez fournir un code IMEI valide'
        },
        imo: {
            'default': 'Veuillez fournir un code IMO valide'
        },
        integer: {
            'default': 'Veuillez fournir un nombre valide'
        },
        ip: {
            'default': 'Veuillez fournir une adresse IP valide',
            ipv4: 'Veuillez fournir une adresse IPv4 valide',
            ipv6: 'Veuillez fournir une adresse IPv6 valide'
        },
        isbn: {
            'default': 'Veuillez fournir un code ISBN valide'
        },
        isin: {
            'default': 'Veuillez fournir un code ISIN valide'
        },
        ismn: {
            'default': 'Veuillez fournir un code ISMN valide'
        },
        issn: {
            'default': 'Veuillez fournir un code ISSN valide'
        },
        lessThan: {
            'default': 'Veuillez fournir une valeur inférieure ou égale à %s',
            notInclusive: 'Veuillez fournir une valeur inférieure à %s'
        },
        mac: {
            'default': 'Veuillez fournir une adresse MAC valide'
        },
        meid: {
            'default': 'Veuillez fournir un code MEID valide'
        },
        notEmpty: {
            'default': 'Veuillez fournir une valeur'
        },
        numeric: {
            'default': 'Veuillez fournir une valeur décimale valide'
        },
        phone: {
            'default': 'Veuillez fournir un numéro de téléphone valide',
            countryNotSupported: 'Le code de pays %s n\'est pas accepté',
            country: 'Veuillez fournir un numéro de téléphone valide pour %s',
            countries: {
                BR: 'Brésil',
                CN: 'Chine',
                CZ: 'République Tchèque',
                DE: 'Allemagne',
                DK: 'Danemark',
                ES: 'Espagne',
                FR: 'France',
                GB: 'Royaume-Uni',
                MA: 'Maroc',
                PK: 'Pakistan',
                RO: 'Roumanie',
                RU: 'Russie',
                SK: 'Slovaquie',
                TH: 'Thaïlande',
                US: 'USA',
                VE: 'Venezuela'
            }
        },
        regexp: {
            'default': 'Veuillez fournir une valeur correspondant au modèle'
        },
        remote: {
            'default': 'Veuillez fournir une valeur valide'
        },
        rtn: {
            'default': 'Veuillez fournir un code RTN valide'
        },
        sedol: {
            'default': 'Veuillez fournir a valid SEDOL number'
        },
        siren: {
            'default': 'Veuillez fournir un numéro SIREN valide'
        },
        siret: {
            'default': 'Veuillez fournir un numéro SIRET valide'
        },
        step: {
            'default': 'Veuillez fournir un écart valide de %s'
        },
        stringCase: {
            'default': 'Veuillez ne fournir que des caractères minuscules',
            upper: 'Veuillez ne fournir que des caractères majuscules'
        },
        stringLength: {
            'default': 'Veuillez fournir une valeur de longueur valide',
            less: 'Veuillez fournir moins de %s caractères',
            more: 'Veuillez fournir plus de %s caractères',
            between: 'Veuillez fournir entre %s et %s caractères'
        },
        uri: {
            'default': 'Veuillez fournir un URI valide'
        },
        uuid: {
            'default': 'Veuillez fournir un UUID valide',
            version: 'Veuillez fournir un UUID version %s number'
        },
        vat: {
            'default': 'Veuillez fournir un code VAT valide',
            countryNotSupported: 'Le code de pays %s n\'est pas accepté',
            country: 'Veuillez fournir un code VAT valide pour %s',
            countries: {
                AT: 'Autriche',
                BE: 'Belgique',
                BG: 'Bulgarie',
                BR: 'Brésil',
                CH: 'Suisse',
                CY: 'Chypre',
                CZ: 'République Tchèque',
                DE: 'Allemagne',
                DK: 'Danemark',
                EE: 'Estonie',
                ES: 'Espagne',
                FI: 'Finlande',
                FR: 'France',
                GB: 'Royaume-Uni',
                GR: 'Grèce',
                EL: 'Grèce',
                HU: 'Hongrie',
                HR: 'Croatie',
                IE: 'Irlande',
                IS: 'Islande',
                IT: 'Italie',
                LT: 'Lituanie',
                LU: 'Luxembourg',
                LV: 'Lettonie',
                MT: 'Malte',
                NL: 'Pays-Bas',
                NO: 'Norvège',
                PL: 'Pologne',
                PT: 'Portugal',
                RO: 'Roumanie',
                RU: 'Russie',
                RS: 'Serbie',
                SE: 'Suède',
                SI: 'Slovénie',
                SK: 'Slovaquie',
                VE: 'Venezuela',
                ZA: 'Afrique du Sud'
            }
        },
        vin: {
            'default': 'Veuillez fournir un code VIN valide'
        },
        zipCode: {
            'default': 'Veuillez fournir un code postal valide',
            countryNotSupported: 'Le code de pays %s n\'est pas accepté',
            country: 'Veuillez fournir un code postal valide pour %s',
            countries: {
                AT: 'Autriche',
                BR: 'Brésil',
                CA: 'Canada',
                CH: 'Suisse',
                CZ: 'République Tchèque',
                DE: 'Allemagne',
                DK: 'Danemark',
                FR: 'France',
                GB: 'Royaume-Uni',
                IE: 'Irlande',
                IT: 'Italie',
                MA: 'Maroc',
                NL: 'Pays-Bas',
                PT: 'Portugal',
                RO: 'Roumanie',
                RU: 'Russie',
                SE: 'Suède',
                SG: 'Singapour',
                SK: 'Slovaquie',
                US: 'USA'
            }
        }
    });
}(window.jQuery));
