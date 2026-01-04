//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package dev.langchain4j.model.chat.response;

import dev.langchain4j.Experimental;
import dev.langchain4j.internal.Utils;
import dev.langchain4j.internal.ValidationUtils;

import java.util.Objects;

@Experimental
public class PartialToolCall {
    private final int index;
    private final String id;
    private final String name;
    private final String partialArguments;

    public PartialToolCall(Builder builder) {
        this.index = ValidationUtils.ensureNotNegative(builder.index, "index");
        this.id = builder.id;
        this.name = ValidationUtils.ensureNotBlank(builder.name, "name");
        this.partialArguments = builder.partialArguments;
    }

    public int index() {
        return this.index;
    }

    public String id() {
        return this.id;
    }

    public String name() {
        return this.name;
    }

    public String partialArguments() {
        return this.partialArguments;
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        } else if (object != null && this.getClass() == object.getClass()) {
            PartialToolCall that = (PartialToolCall) object;
            return this.index == that.index && Objects.equals(this.id, that.id) && Objects.equals(this.name, that.name) && Objects.equals(this.partialArguments, that.partialArguments);
        } else {
            return false;
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.index, this.id, this.name, this.partialArguments});
    }

    public String toString() {
        int var10000 = this.index;
        return "PartialToolCall{index=" + var10000 + ", id=" + Utils.quoted(this.id) + ", name=" + Utils.quoted(this.name) + ", partialArguments=" + Utils.quoted(this.partialArguments) + "}";
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int index;
        private String id;
        private String name;
        private String partialArguments;

        public Builder() {
        }

        public Builder index(int index) {
            this.index = index;
            return this;
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder partialArguments(String partialArguments) {
            this.partialArguments = partialArguments;
            return this;
        }

        public PartialToolCall build() {
            return new PartialToolCall(this);
        }
    }
}