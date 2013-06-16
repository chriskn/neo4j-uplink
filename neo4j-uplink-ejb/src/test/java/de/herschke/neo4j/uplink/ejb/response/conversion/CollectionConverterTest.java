package de.herschke.neo4j.uplink.ejb.response.conversion;

import java.util.List;
import org.apache.commons.beanutils.ConvertUtils;
import org.fest.assertions.Assertions;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class CollectionConverterTest {

    private enum MyEnum {

        BlaBla;
    }

    @Test
    public void testStringToListConversion() {
        ConvertUtils.deregister(List.class);
        ConvertUtils.register(new CollectionConverter(MyEnum.class), List.class);
        Assertions.assertThat((List) ConvertUtils.convert("blabla",
                List.class)).contains(MyEnum.BlaBla);
    }
}
