package com.nexr.newsfeed.objs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.std.StdKeySerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.google.cloud.logging.Payload;
import com.google.cloud.logging.Severity;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.logging.type.LogSeverity;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by seoeun on 11/22/16.
 */
public class MonitoringEvent implements Serializable {

    public static final String NOTIFICATION_CHANNEL = "monitor";

    public static final String LABEL_KEY_SERVCIE = "service";
    public static final String LABEL_KEY_REQUESTID = "requestId";
    public static final String STRUCT_KEY_MESSAGE = "message";
    public static final String STRUCT_KEY_EXCEPTION = "exception";

    // TODO, configurable.
    public static final int EXCEPTION_STACK_DEPTH = -1;
    private Service service = Service.PAYMENT;
    private Payload.JsonPayload payload = Payload.JsonPayload.of(new HashMap<String, Object>());
    @JsonIgnore
    private Map<String, String> labels;
    private Level level = Level.INFO;
    @JsonProperty(value = "timestamp")
    private long timestamp;
    @JsonProperty
    private String requestId;
    @JsonProperty
    private String message;
    private Throwable throwable;
    private String exception;
    private String viewUrl;

    MonitoringEvent() {

    }

    MonitoringEvent(Builder builder) {
        this.level = builder.level;
        this.timestamp = builder.timestamp;
        this.service = builder.service;
        this.requestId = builder.requestId == null ? "" : builder.requestId; // NotNullable
        this.message = builder.message;
        this.throwable = builder.throwable;
        this.viewUrl = builder.viewUrl;
        this.exception = MonitoringEvent.stackTrace(throwable, EXCEPTION_STACK_DEPTH);
        this.labels = ImmutableMap.of(LABEL_KEY_SERVCIE, service.name(),
                LABEL_KEY_REQUESTID, this.requestId);
//        this.labels = (Map<String, String>) MonitoringService.mapOf(LABEL_KEY_SERVCIE, service.name(),
//                LABEL_KEY_REQUESTID, this.requestId);

        Map<String, Object> data = new HashMap<>();
        if (builder.data != null) {
            data.putAll(builder.data);
        }
        data.put(STRUCT_KEY_MESSAGE, this.message);
        if (throwable != null) {
            data.put(STRUCT_KEY_EXCEPTION, exception);
        }
        payload = Payload.JsonPayload.of(data);
    }

    public static String stackTrace(Throwable e, int size) {
        if (e == null) {
            return "";
        }
        StackTraceElement[] stackTraceElements = e.getStackTrace();
        int max = size < 0 ? stackTraceElements.length : size;
        max = Math.min(max, stackTraceElements.length);
        StringBuilder builder = new StringBuilder(e.toString() + "\n");
        for (int i = 0; i < max; i++) {
            builder.append("\tat " + stackTraceElements[i] + "\n");
        }
        return builder.toString();
    }

    public Level getLevel() {
        return level;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Service getService() {
        return service;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public String getException() {
        return exception;
    }

    @JsonIgnore
    public Map<String, String> getLabels() {
        return labels;
    }

    public Payload.JsonPayload getPayload() {
        return payload;
    }

    public String getViewUrl() {
        return viewUrl;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", timestamp)
                .add("service", service.name())
                .add("requestId", requestId)
                .add("level", level.name())
                .add("labels", labels)
                .add("payload", payload)
                .toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MonitoringEvent)) {
            return false;
        }
        MonitoringEvent other = (MonitoringEvent) obj;
        return Objects.equals(level, other.level)
                && Objects.equals(timestamp, other.timestamp)
                && Objects.equals(service, other.service)
                && Objects.equals(requestId, other.requestId)
                && Objects.equals(message, other.message)
                && Objects.equals(throwable, other.throwable)
                && Objects.equals(exception, other.exception)
                && Objects.equals(viewUrl, other.viewUrl)
                && Objects.equals(payload, other.payload)
                && Objects.equals(labels, other.labels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(level, timestamp, service, requestId, message, throwable, exception, viewUrl, payload, labels);
    }

    public enum Level {
        INFO {
            @Override
            public Severity getServerity() {
                return Severity.INFO;
            }

            @Override
            public String getColor() {
                return "good";
            }
        },
        ERROR {
            @Override
            public Severity getServerity() {
                return Severity.ERROR;
            }

            @Override
            public String getColor() {
                return "danger";
            }
        };

        public abstract Severity getServerity();

        public abstract String getColor();

        public LogSeverity convert(Severity severity) {
            if (severity == Severity.ERROR) {
                return LogSeverity.ERROR;
            }
            return LogSeverity.INFO;
        }
    }

    public enum Service {

        PAYMENT,
        PURCHASE,
        LOGIN,
        SIGNUP,
        VIEWER;

        public String getChannel() {
            return MonitoringEvent.NOTIFICATION_CHANNEL + "-" + "test";
        }
    }

    public static class Builder {

        private final Service service;
        private final String message;
        private Level level = Level.INFO;
        private long timestamp;
        private String requestId;
        private Throwable throwable;
        private Map<String, Object> data;
        private String viewUrl;

        public Builder(MonitoringEvent event) {
            this.level = event.level;
            this.timestamp = event.timestamp;
            this.service = event.service;
            this.requestId = event.requestId;
            this.message = event.message;
            this.throwable = event.throwable;
            this.viewUrl = event.viewUrl;
            this.data = new HashMap<>();
            data.putAll(event.getPayload().getDataAsMap());
            data.remove(STRUCT_KEY_MESSAGE);
            data.remove(STRUCT_KEY_EXCEPTION);
        }

        public Builder(Service service, String message) {
            this.service = service;
            this.message = message;
        }

        public Builder setTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public Builder setRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder setThrowable(Throwable throwable) {
            this.throwable = throwable;
            return this;
        }

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder setData(Map<String, Object> data) {
            this.data = data;
            return this;
        }

        public Builder setViewUrl(String viewUrl) {
            this.viewUrl = viewUrl;
            return this;
        }

        public MonitoringEvent build() {
            return new MonitoringEvent(this);
        }
    }

    public class MapKeySerializer extends StdSerializer<Object> {
        private StdSerializer<Object> DEFAULT = new StdKeySerializer();
        private ObjectMapper mapper = new ObjectMapper();

        protected MapKeySerializer(Class<Object> t) {
            super(t);
        }

        @Override
        public void serialize(Object o, com.fasterxml.jackson.core.JsonGenerator jsonGenerator, com.fasterxml.jackson.databind.SerializerProvider serializerProvider) throws IOException, com.fasterxml.jackson.core.JsonGenerationException {

        }

//        @Override
//        public JsonNode getSchema(SerializerProvider provider, Type typeHint) throws JsonMappingException {
//            DEFAULT.getSchema()
//            return DEFAULT.getSchema(provider, typeHint);
//        }


    }
}
