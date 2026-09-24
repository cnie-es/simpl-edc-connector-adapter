package eu.europa.ec.simpl.edcconnectoradapter;

public class TestConstants {

    public static final String CATALOG_RESPONSE_ONE_OFFER_CONTRACT_DEFINITION_ID =
            "f7e3aab7-7858-4ae2-90b8-08dffc531ecd";

    public static final String CATALOG_RESPONSE_ONE_OFFER =
            """
              {
              	"@id": "e5674c61-2808-44c5-9f32-a1dcb66020bd",
              	"@type": "dcat:Catalog",
              	"dcat:dataset": {
              		"@id": "ddb635ae-7ee7-44a5-813a-06b529702c0e-1",
              		"@type": "dcat:Dataset",
              		"odrl:hasPolicy": {
              			"@id": "ZjdlM2FhYjctNzg1OC00YWUyLTkwYjgtMDhkZmZjNTMxZWNk:ZGRiNjM1YWUtN2VlNy00NGE1LTgxM2EtMDZiNTI5NzAyYzBlLTE=:NmMxODNlZWQtZTJmMi00YTg4LWE4MzItMjgwZjg0NDkxOWEy",
              			"@type": "odrl:Set",
              			"odrl:permission": [],
              			"odrl:prohibition": [],
              			"odrl:obligation": [],
                          "odrl:assigner": "provider",
                          "odrl:target": {
                              "@id": "c249e7b1-6b0d-4ec1-8fca-73c43427301c"
                          }
              		},
              		"dcat:distribution": [
              			{
              				"@type": "dcat:Distribution",
              				"dct:format": {
              					"@id": "IonosS3-PUSH"
              				},
              				"dcat:accessService": "f2a7f6e9-225b-4ac9-96bf-37f3fe4cde4c"
              			}
              		],
              		"edc:id": "ddb635ae-7ee7-44a5-813a-06b529702c0e-1"
              	},
              	"dcat:service": {
              		"@id": "f2a7f6e9-225b-4ac9-96bf-37f3fe4cde4c",
              		"@type": "dcat:DataService",
              		"dct:terms": "connector",
              		"dct:endpointUrl": "http://testhost/api/v1/dsp"
              	},
              	"edc:participantId": "provider",
              	"@context": {
              		"dct": "https://purl.org/dc/terms/",
              		"edc": "https://w3id.org/edc/v0.0.1/ns/",
              		"dcat": "https://www.w3.org/ns/dcat/",
              		"odrl": "http://www.w3.org/ns/odrl/2/",
              		"dspace": "https://w3id.org/dspace/v0.8/"
              	}
              }
            """;

    public static final String CATALOG_RESPONSE_MULTIPLE_OFFERS =
            """
            {
            	"@id": "e5674c61-2808-44c5-9f32-a1dcb66020bd",
            	"@type": "dcat:Catalog",
            	"dcat:dataset": {
            		"@id": "c249e7b1-6b0d-4ec1-8fca-73c43427301c",
            		"@type": "dcat:Dataset",
            		"odrl:hasPolicy": [
            			{
            				"@id": "NmFmMTYwYTgtNDNlMC00ZDQxLThiNTYtZDNmN2UyMDM2NDMw:YzI0OWU3YjEtNmIwZC00ZWMxLThmY2EtNzNjNDM0MjczMDFj:NWU5Y2Q1NGItODA4NC00ODYxLWJkNTctMDU1NGMxYzE2ODdl",
            				"@type": "odrl:Offer",
            				"odrl:permission": [],
            				"odrl:prohibition": [],
            				"odrl:obligation": [],
            				"odrl:assigner": "provider",
            				"odrl:target": {
            					"@id": "c249e7b1-6b0d-4ec1-8fca-73c43427301c"
            				}
            			},
            			{
            				"@id": "MTMyZjdlY2MtNGJiMC00NGM4LThiMmUtNDY0NjNjYTVkZmNh:YzI0OWU3YjEtNmIwZC00ZWMxLThmY2EtNzNjNDM0MjczMDFj:YTgxOWIwZjEtYjRlNC00YmRiLTliZmYtOWU2NWJhNTUzMmFk",
            				"@type": "odrl:Offer",
            				"odrl:permission": [],
            				"odrl:prohibition": [],
            				"odrl:obligation": [],
            				"odrl:assigner": "provider",
            				"odrl:target": {
            					"@id": "c249e7b1-6b0d-4ec1-8fca-73c43427301c"
            				}
            			}
            		],
            		"dcat:distribution": [
            			{
            				"@type": "dcat:Distribution",
            				"dct:format": {
            					"@id": "IonosS3-PUSH"
            				},
            				"dcat:accessService": "f2a7f6e9-225b-4ac9-96bf-37f3fe4cde4c"
            			}
            		],
            		"edc:id": "ddb635ae-7ee7-44a5-813a-06b529702c0e-1"
            	},
            	"dcat:service": {
            		"@id": "f2a7f6e9-225b-4ac9-96bf-37f3fe4cde4c",
            		"@type": "dcat:DataService",
            		"dct:terms": "connector",
            		"dct:endpointUrl": "http://testhost/api/v1/dsp"
            	},
            	"edc:participantId": "provider",
            	"@context": {
            		"dct": "https://purl.org/dc/terms/",
            		"edc": "https://w3id.org/edc/v0.0.1/ns/",
            		"dcat": "https://www.w3.org/ns/dcat/",
            		"odrl": "http://www.w3.org/ns/odrl/2/",
            		"dspace": "https://w3id.org/dspace/v0.8/"
            	}
            }
            """;

    public static final String EMPTY_CATALOG_RESPONSE =
            """
                    {
                        "@id": "d03a45c9-3a48-403b-a184-a54f526b1589",
                        "@type": "dcat:Catalog",
                        "dcat:dataset": [],
                        "dcat:distribution": [],
                        "dcat:service": {
                            "@id": "650a7c0b-ebfc-475c-b115-e0287655123c",
                            "@type": "dcat:DataService",
                            "dcat:endpointDescription": "dspace:connector",
                            "dcat:endpointUrl": "http://testhost/api/v1/dsp"
                        },
                        "dspace:participantId": "provider",
                        "@context": {
                            "dcat": "http://www.w3.org/ns/dcat#",
                            "dct": "http://purl.org/dc/terms/",
                            "odrl": "http://www.w3.org/ns/odrl/2/",
                            "dspace": "https://w3id.org/dspace/v0.8/",
                            "@vocab": "https://w3id.org/edc/v0.0.1/ns/",
                            "edc": "https://w3id.org/edc/v0.0.1/ns/"
                        }
                    }
                    """;

    public static final String OFFER_POLICY =
            """
            {
            	"@id": "YzI3ZjQwNjQtYTg4Ny00ZWQwLWE4MDItNGE0MjNmZWI4MWNm:ZDIyYTNlYzUtZDc3NS00MjE1LWI4YTQtNzVlODRkYzVjMWM0:Njc5Y2Y4MGQtZTcyOS00ZWYyLWJjNzctMDY1OTE5OGVjMGZh",
            	"@type": "odrl:Offer",
            	"odrl:permission": {
            		"odrl:action": {
            			"@id": "odrl:use"
            		},
            		"odrl:constraint": [
            			{
            				"odrl:leftOperand": {
            					"@id": "odrl:count"
            				},
            				"odrl:operator": {
            					"@id": "odrl:lteq"
            				},
            				"odrl:rightOperand": "10"
            			},
            			{
            				"odrl:leftOperand": {
            					"@id": "odrl:dateTime"
            				},
            				"odrl:operator": {
            					"@id": "odrl:gteq"
            				},
            				"odrl:rightOperand": "2024-08-01T00:00:00Z"
            			},
            			{
            				"odrl:leftOperand": {
            					"@id": "odrl:dateTime"
            				},
            				"odrl:operator": {
            					"@id": "odrl:lteq"
            				},
            				"odrl:rightOperand": "2024-08-31T23:59:59Z"
            			}
            		]
            	},
            	"odrl:prohibition": [],
            	"odrl:obligation": [],
            	"odrl:assigner": "provider",
            	"odrl:target": { "@id": "d22a3ec5-d775-4215-b8a4-75e84dc5c1c4" }
            }
            """;
}
