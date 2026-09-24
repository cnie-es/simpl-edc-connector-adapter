package eu.europa.ec.simpl.edcconnectoradapter.util;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.Constraint;
import eu.europa.ec.simpl.data1.common.adapter.connector.model.contract.ConstraintOperator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class JsonNodeObjectMapperUtil {

    private JsonNodeObjectMapperUtil() {}

    public static List<Constraint> getValuesInJsonUsingJsonParser(JsonNode constraintsValues) {
        List<Constraint> values = new ArrayList<>();
        if (constraintsValues == null || constraintsValues.isNull()) {
            return values;
        } else if (constraintsValues.isArray()) {
            constraintsValues.forEach(ele -> values.add(mapConstraint(ele)));
        } else {
            values.add(mapConstraint(constraintsValues));
        }
        return values;
    }

    private static Constraint mapConstraint(JsonNode constraint) {
        return Constraint.builder()
                .leftOperand(
                        EDCPrefixMapperUtil.formatOdrlValue(getJsonValueFromObject(constraint, "/odrl:leftOperand")))
                .operator(ConstraintOperator.valueOf(
                        EDCPrefixMapperUtil.formatOdrlValue(getJsonValueFromObject(constraint, "/odrl:operator"))
                                .toUpperCase(Locale.getDefault())))
                .rightOperand(getJsonValueFromObject(constraint, "/odrl:rightOperand"))
                .build();
    }

    public static String getJsonValueFromObject(JsonNode jsonNode, String property) {
        JsonNode jsonObject = jsonNode.at(property);
        if (jsonObject.isObject()) {
            return getJsonValue(jsonObject, "/@id");
        } else {
            return getJsonValue(jsonNode, property);
        }
    }

    public static String getJsonValue(JsonNode jsonNode, String propertyName) {
        JsonNode propertyNode = jsonNode.at(propertyName);
        if (propertyNode != null) {
            return propertyNode.asText();
        }
        return "";
    }
}
