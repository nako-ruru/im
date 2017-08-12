package com.ijimu.capital;

import com.google.common.base.Equivalence;
import com.google.common.collect.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/8/9.
 */
public class BlackKeywordSerializationTest {

    @Test
    public void test1() throws IOException, ClassNotFoundException {

        BlackKeyword blackKeyword = new BlackKeyword();
        blackKeyword.firstCharHash = new HashMap<>();
        put(blackKeyword.firstCharHash, "A", "A1", "A2", "A3");
        put(blackKeyword.firstCharHash, "B", "B1", "B2", "B3", "B4");
        put(blackKeyword.firstCharHash, "C", "C1", "C2", "C3", "C4", "C5");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(blackKeyword);
        oos.flush();

        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        BlackKeyword newBlackKeyword = (BlackKeyword) ois.readObject();

        MapDifference<String, BlackKeyword.BlackWordHashNode> diff = Maps.difference(blackKeyword.firstCharHash, newBlackKeyword.firstCharHash, new Equivalence<BlackKeyword.BlackWordHashNode>() {
            @Override
            protected boolean doEquivalent(BlackKeyword.BlackWordHashNode n1, BlackKeyword.BlackWordHashNode n2) {
                Multiset<String> m1 = collect(n1);
                Multiset<String> m2 = collect(n2);
                Sets.SetView<String> nDiff = Sets.symmetricDifference(m1.elementSet(), m2.elementSet());
                return nDiff.isEmpty();
            }

            @Override
            protected int doHash(BlackKeyword.BlackWordHashNode v) {
                Multiset<String> m = collect(v);
                return m.elementSet().hashCode();
            }
        });

        Assert.assertTrue(diff.areEqual());
    }

    private static void put(Map<String, BlackKeyword.BlackWordHashNode> map, String key, String... values) {
        BlackKeyword.BlackWordHashNode first = null;
        BlackKeyword.BlackWordHashNode p = null;
        for(String v : values) {
            BlackKeyword.BlackWordHashNode n = new BlackKeyword.BlackWordHashNode();
            n.setText(v);
            if(first == null) {
                first = n;
            } else {
                p.setNext(n);
            }
            p = n;
        }
        map.put(key, first);
    }

    private static Multiset<String> collect(BlackKeyword.BlackWordHashNode n) {
        Multiset<String> result = HashMultiset.create();
        while(n != null) {
            result.add(n.getText());
            n = n.getNext();
        }
        return result;
    }

}
