package run.halo.app.core.extension.reconciler;

import java.util.HashMap;
import java.util.Map;
import run.halo.app.core.extension.Reply;
import run.halo.app.extension.ExtensionClient;
import run.halo.app.extension.controller.Reconciler;
import run.halo.app.infra.utils.JsonUtils;

/**
 * Reply reconciler.
 *
 * @author guqing
 * @since 2.0.0
 */
public class ReplyReconciler implements Reconciler<Reconciler.Request> {

    private final ExtensionClient client;

    public ReplyReconciler(ExtensionClient client) {
        this.client = client;
    }

    @Override
    public Result reconcile(Request request) {
        client.fetch(Reply.class, request.name())
            .ifPresent(reply -> {
                reconcileMetadata(request.name());
            });
        return new Result(false, null);
    }

    private void reconcileMetadata(String name) {
        client.fetch(Reply.class, name).ifPresent(reply -> {
            final Reply oldReply = JsonUtils.deepCopy(reply);
            Map<String, String> labels = getLabelsNullSafe(reply);
            labels.put(Reply.COMMENT_NAME_LABEL, reply.getSpec().getCommentName());
            labels.put(Reply.QUOTE_REPLY_LABEL, reply.getSpec().getQuoteReply());
            if (!oldReply.equals(reply)) {
                client.update(reply);
            }
        });
    }

    private static Map<String, String> getLabelsNullSafe(Reply reply) {
        Map<String, String> labels = reply.getMetadata().getLabels();
        if (labels == null) {
            labels = new HashMap<>();
            reply.getMetadata().setLabels(labels);
        }
        return labels;
    }

}
