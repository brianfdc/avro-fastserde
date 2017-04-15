package com.rtbhouse.utils.avro;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericContainer;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.codehaus.jackson.node.NullNode;

public final class FastSerdeTestsSupport {

    private FastSerdeTestsSupport() {
    }

    public static Schema createRecord(String name, Schema.Field... fields) {
        Schema schema = Schema.createRecord(name, name, "com.adpilot.utils.generated.avro", false);
        schema.setFields(Arrays.asList(fields));

        return schema;
    }

    public static Schema.Field createField(String name, Schema schema) {
        return new Schema.Field(name, schema, "", null, Schema.Field.Order.ASCENDING);
    }

    public static Schema.Field createUnionField(String name, Schema... schemas) {
        List<Schema> typeList = new ArrayList<>();
        typeList.add(Schema.create(Schema.Type.NULL));
        typeList.addAll(Arrays.asList(schemas));

        Schema unionSchema = Schema.createUnion(typeList);
        return new Schema.Field(name, unionSchema, null, NullNode.getInstance(), Schema.Field.Order.ASCENDING);
    }

    public static Schema.Field createPrimitiveFieldSchema(String name, Schema.Type type) {
        return new Schema.Field(name, Schema.create(type), null, null);
    }

    public static Schema.Field createPrimitiveUnionFieldSchema(String name, Schema.Type... types) {
        List<Schema> typeList = new ArrayList<>();
        typeList.add(Schema.create(Schema.Type.NULL));
        typeList.addAll(Arrays.asList(types).stream().map(Schema::create).collect(Collectors.toList()));

        Schema unionSchema = Schema.createUnion(typeList);
        return new Schema.Field(name, unionSchema, null, NullNode.getInstance(), Schema.Field.Order.ASCENDING);
    }

    public static Schema.Field createArrayFieldSchema(String name, Schema elementType, String... aliases) {
        return addAliases(new Schema.Field(name, Schema.createArray(elementType), null, null,
            Schema.Field.Order.ASCENDING), aliases);
    }

    public static Schema.Field createMapFieldSchema(String name, Schema valueType, String... aliases) {
        return addAliases(new Schema.Field(name, Schema.createMap(valueType), null, null,
            Schema.Field.Order.ASCENDING), aliases);
    }

    public static Schema createFixedSchema(String name, int size) {
        return Schema.createFixed(name, "", "com.adpilot.utils.generated.avro", size);
    }

    public static Schema createEnumSchema(String name, String[] ordinals) {
        return Schema.createEnum(name, "", "com.adpilot.utils.generated.avro", Arrays.asList(ordinals));
    }

    public static Schema createUnionSchema(Schema... schemas) {
        List<Schema> typeList = new ArrayList<>();
        typeList.add(Schema.create(Schema.Type.NULL));
        typeList.addAll(Arrays.asList(schemas));

        return Schema.createUnion(typeList);
    }

    public static Schema.Field addAliases(Schema.Field field, String... aliases) {
        if (aliases != null) {
            Arrays.asList(aliases).forEach(field::addAlias);
        }

        return field;
    }

    public static <T extends GenericContainer> Decoder genericDataAsDecoder(T data) {
        return genericDataAsDecoder(data, data.getSchema());
    }

    public static <T> Decoder genericDataAsDecoder(T data, Schema schema) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryEncoder binaryEncoder = EncoderFactory.get().directBinaryEncoder(baos, null);

        try {
            GenericDatumWriter<T> writer = new GenericDatumWriter<>(schema);
            writer.write(data, binaryEncoder);
            binaryEncoder.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return DecoderFactory.get().binaryDecoder(baos.toByteArray(), null);
    }

    public static <T extends SpecificRecord> Decoder specificDataAsDecoder(T record) {
        return specificDataAsDecoder(record, record.getSchema());
    }

    public static <T> Decoder specificDataAsDecoder(T record, Schema schema) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BinaryEncoder binaryEncoder = EncoderFactory.get().directBinaryEncoder(baos, null);

        try {
            SpecificDatumWriter<T> writer = new SpecificDatumWriter<>(schema);
            writer.write(record, binaryEncoder);
            binaryEncoder.flush();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return DecoderFactory.get().binaryDecoder(baos.toByteArray(), null);
    }


}