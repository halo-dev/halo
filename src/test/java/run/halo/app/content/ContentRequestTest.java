package run.halo.app.content;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import run.halo.app.core.extension.Snapshot;
import run.halo.app.infra.utils.JsonUtils;

/**
 * @author guqing
 * @since 2.0.0
 */
class ContentRequestTest {
    private ContentRequest contentRequest;

    @BeforeEach
    void setUp() {
        contentRequest = new ContentRequest("""
            Four score and seven
            years ago our fathers

            brought forth on this continent
            """,
            """
                <p>Four score and seven</p>
                <p>years ago our fathers</p>
                <br/>
                <p>brought forth on this continent</p>
                """,
            "MARKDOWN");
    }

    @Test
    void toSnapshot() throws JSONException {
        Snapshot snapshot = contentRequest.toSnapshot();
        snapshot.getMetadata().setName("7b149646-ac60-4a5c-98ee-78b2dd0631b2");
        JSONAssert.assertEquals(JsonUtils.objectToJson(snapshot),
            """
                    {
                        "spec": {
                            "subjectRef": {
                                "kind": null,
                                "name": null
                            },
                            "rawType": "MARKDOWN",
                            "rawPatch": "Four score and seven\\nyears ago our fathers\\n\\nbrought forth on this continent\\n",
                            "contentPatch": "<p>Four score and seven</p>\\n<p>years ago our fathers</p>\\n<br/>\\n<p>brought forth on this continent</p>\\n",
                            "parentSnapshotName": null,
                            "displayVersion": "v1",
                            "version": 1,
                            "publishTime": null
                        },
                        "apiVersion": "content.halo.run/v1alpha1",
                        "kind": "Snapshot",
                        "metadata": {
                            "name": "7b149646-ac60-4a5c-98ee-78b2dd0631b2",
                            "labels": null,
                            "annotations": null,
                            "version": null,
                            "creationTimestamp": null,
                            "deletionTimestamp": null
                        }
                    }
                """, true);
    }

    @Test
    void rawPatchFrom() throws JSONException {
        String s = contentRequest.rawPatchFrom("""
            Four score and seven
            years ago our fathers
            """);
        JSONAssert.assertEquals(s,
            """
                   [
                    {
                        "source": {
                            "position": 3,
                            "lines": [],
                            "changePosition": null
                        },
                        "target": {
                            "position": 3,
                            "lines": [
                                "brought forth on this continent",
                                ""
                            ],
                            "changePosition": null
                        },
                        "type": "INSERT"
                    }
                ]
                """, true);
    }

    @Test
    void contentPatchFrom() throws JSONException {
        String s = contentRequest.contentPatchFrom("""
            <p>Four score and seven</p>
            <p>years ago our fathers</p>
            """);
        JSONAssert.assertEquals(s, """
            [
                 {
                     "source": {
                         "position": 2,
                         "lines": [],
                         "changePosition": null
                     },
                     "target": {
                         "position": 2,
                         "lines": [
                             "<br/>",
                             "<p>brought forth on this continent</p>"
                         ],
                         "changePosition": null
                     },
                     "type": "INSERT"
                 }
            ]
            """, true);
    }
}