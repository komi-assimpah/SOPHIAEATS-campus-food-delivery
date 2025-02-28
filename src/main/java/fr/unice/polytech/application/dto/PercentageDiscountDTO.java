package fr.unice.polytech.application.dto;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fr.unice.polytech.domain.models.restaurant.discountstrategy.PercentageDiscount;

import java.io.IOException;

public class  PercentageDiscountDTO extends JsonSerializer<PercentageDiscount>{

    @Override
    public void serialize(PercentageDiscount value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("percentage", value.getPercentage());
        gen.writeNumberField("quantity", value.getQuantity());
        gen.writeEndObject();
    }

}