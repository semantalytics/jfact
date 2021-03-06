package jfact.unittest;

import static org.mockito.Mockito.*;

import org.junit.Test;

import uk.ac.manchester.cs.jfact.helpers.DLVertex;
import uk.ac.manchester.cs.jfact.helpers.LogAdapter;
import uk.ac.manchester.cs.jfact.kernel.DLDag;
import uk.ac.manchester.cs.jfact.kernel.DlCompletionTree;
import uk.ac.manchester.cs.jfact.kernel.modelcaches.*;

@SuppressWarnings({ "rawtypes", "unused", "unchecked", "javadoc" })
public class GeneratedJUnitTest_uk_ac_manchester_cs_jfact_kernel_modelcaches {
    @Test
    public void shouldTestModelCacheConst() throws Exception {
        ModelCacheConst testSubject0 = new ModelCacheConst(mock(boolean.class));
        ModelCacheState result0 = testSubject0.getState();
        ModelCacheState result1 = testSubject0.canMerge(mock(ModelCacheInterface.class));
        ModelCacheType result2 = testSubject0.getCacheType();
        testSubject0.logCacheEntry(mock(int.class), mock(LogAdapter.class));
        ModelCacheConst result3 = ModelCacheConst.createConstCache(mock(int.class));
        boolean result4 = testSubject0.getConst();
        testSubject0.setHasNominalNode(mock(boolean.class));
        boolean result5 = testSubject0.hasNominalClash(mock(ModelCacheInterface.class));
        boolean result6 = testSubject0.isHasNominalNode();
        testSubject0.updateNominalStatus(mock(ModelCacheInterface.class));
        boolean result7 = testSubject0.shallowCache();
        String result8 = testSubject0.toString();
    }

    @Test
    public void shouldTestModelCacheIan() throws Exception {
        ModelCacheIan testSubject0 = new ModelCacheIan(mock(DLDag.class),
                mock(DlCompletionTree.class), mock(boolean.class), mock(int.class),
                mock(int.class), mock(boolean.class));
        ModelCacheIan testSubject1 = new ModelCacheIan(mock(boolean.class),
                mock(int.class), mock(int.class), mock(boolean.class));
        testSubject0.clear();
        ModelCacheState result0 = testSubject0.getState();
        ModelCacheState result1 = testSubject0.merge(mock(ModelCacheInterface.class));
        ModelCacheState result2 = testSubject0.canMerge(mock(ModelCacheInterface.class));
        ModelCacheType result3 = testSubject0.getCacheType();
        boolean result4 = testSubject0.shallowCache();
        testSubject0.logCacheEntry(mock(int.class), mock(LogAdapter.class));
        testSubject0.initRolesFromArcs(mock(DlCompletionTree.class));
        testSubject0.processConcept(mock(DLVertex.class), mock(boolean.class),
                mock(boolean.class));
        testSubject0.processAutomaton(mock(DLVertex.class));
        ModelCacheState result5 = testSubject0.isMergableSingleton(mock(int.class),
                mock(boolean.class));
        ModelCacheState result6 = testSubject0.isMergableIan(mock(ModelCacheIan.class));
        testSubject0.setHasNominalNode(mock(boolean.class));
        boolean result7 = testSubject0.hasNominalClash(mock(ModelCacheInterface.class));
        boolean result8 = testSubject0.isHasNominalNode();
        testSubject0.updateNominalStatus(mock(ModelCacheInterface.class));
        String result9 = testSubject0.toString();
    }

    @Test
    public void shouldTestModelCacheInterface() throws Exception {
        ModelCacheInterface testSubject0 = null;
        ModelCacheState result0 = testSubject0.getState();
        testSubject0.setHasNominalNode(mock(boolean.class));
        boolean result1 = testSubject0.hasNominalClash(mock(ModelCacheInterface.class));
        boolean result2 = testSubject0.isHasNominalNode();
        testSubject0.updateNominalStatus(mock(ModelCacheInterface.class));
        ModelCacheState result3 = testSubject0.canMerge(mock(ModelCacheInterface.class));
        ModelCacheType result4 = testSubject0.getCacheType();
        boolean result5 = testSubject0.shallowCache();
        testSubject0.logCacheEntry(mock(int.class), mock(LogAdapter.class));
        String result6 = testSubject0.toString();
    }

    @Test
    public void shouldTestModelCacheSingleton() throws Exception {
        ModelCacheSingleton testSubject0 = new ModelCacheSingleton(mock(int.class));
        int result0 = testSubject0.getValue();
        ModelCacheState result1 = testSubject0.getState();
        ModelCacheState result2 = testSubject0.canMerge(mock(ModelCacheInterface.class));
        ModelCacheType result3 = testSubject0.getCacheType();
        testSubject0.logCacheEntry(mock(int.class), mock(LogAdapter.class));
        testSubject0.setHasNominalNode(mock(boolean.class));
        boolean result4 = testSubject0.hasNominalClash(mock(ModelCacheInterface.class));
        boolean result5 = testSubject0.isHasNominalNode();
        testSubject0.updateNominalStatus(mock(ModelCacheInterface.class));
        boolean result6 = testSubject0.shallowCache();
        String result7 = testSubject0.toString();
    }

    @Test
    public void shouldTestModelCacheState() throws Exception {
        ModelCacheState testSubject0 = null;
        ModelCacheState[] result0 = ModelCacheState.values();
        ModelCacheState result1 = ModelCacheState.valueOf(mock(String.class));
        boolean result2 = testSubject0.usageByState();
        String result3 = testSubject0.name();
        String result4 = testSubject0.toString();
        Enum result7 = Enum.valueOf(mock(Class.class), mock(String.class));
        Class result8 = testSubject0.getDeclaringClass();
        int result9 = testSubject0.ordinal();
    }

    @Test
    public void shouldTestModelCacheType() throws Exception {
        ModelCacheType testSubject0 = null;
        ModelCacheType[] result0 = ModelCacheType.values();
        ModelCacheType result1 = ModelCacheType.valueOf(mock(String.class));
        String result2 = testSubject0.name();
        String result3 = testSubject0.toString();
        Enum result6 = Enum.valueOf(mock(Class.class), mock(String.class));
        Class result7 = testSubject0.getDeclaringClass();
        int result8 = testSubject0.ordinal();
    }
}
