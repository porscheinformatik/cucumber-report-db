package at.porscheinformatik.cucumber.mongodb.rest.controller;

import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.springframework.data.mongodb.core.MongoOperations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.google.common.collect.Sets;

public class CollectionControllerTest
{

    private CollectionController collectionController;
    private MongoOperations mongoOperations;

    @BeforeMethod
    public void setUp() throws Exception
    {
        collectionController = new CollectionController();
        mongoOperations = mock(MongoOperations.class);
        Whitebox.setInternalState(collectionController, "mongodb", mongoOperations);
    }

    @Test(dataProvider = "collections_products_and_version")
    public void getCollections_returns_products_and_versions(Set<String> input, Set<String> expectedOutput) throws IOException
    {
        Mockito.when(mongoOperations.getCollectionNames()).thenReturn(input);
        List<String> actualCollections = collectionController.getCollections();
        assertEquals(actualCollections, expectedOutput);
    }

    @DataProvider
    public Object[][] collections_products_and_version() {
        return new Object[][] {
                {Collections.singleton("product_version"), Collections.singleton("product_version")},
                {Collections.singleton("productWithoutVersion"), Collections.emptySet()},
                {Collections.singleton("system.indexes"), Collections.emptySet()},
                {Collections.singleton("product_version.chunks"), Collections.emptySet()},
                {Collections.singleton("product_version.files"), Collections.emptySet()}
        };
    }

    @Test(dataProvider = "products_products_and_version")
    public void getProducts(Set<String> input, Set<String> expectedOutput) throws IOException
    {
        Mockito.when(mongoOperations.getCollectionNames()).thenReturn(input);
        List<String> products = collectionController.getProducts();
        assertEquals(products, expectedOutput);
    }

    @DataProvider
    public Object[][] products_products_and_version() {
        return new Object[][] {
                {Collections.singleton("productA_version"), Collections.singleton("productA")},
                {Sets.newHashSet("productA_version", "productB_version"), Sets.newHashSet("productA", "productB")},
                {Sets.newHashSet("productA_version1","productA_version2", "productB_version"),
                        Sets.newHashSet("productA", "productB")},
        };
    }

}