package org.jflame.toolkit.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jflame.toolkit.excel.ExcelAccessException;
import org.jflame.toolkit.excel.ExcelCreator;
import org.jflame.toolkit.excel.ExcelImportor;
import org.jflame.toolkit.test.entity.Pet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class ExcelTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ExcelTest(String testName) {
        super(testName);
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(ExcelTest.class);
    }

    /**
     * 实体类导出到文件流
     */
    @org.junit.Test
    public void testExport() {
        List<Pet> pets = new ArrayList<>(5);
        for (int i = 0; i < 300; i++) {
            Pet p = new Pet("pet " + i, i + 20, "肤色 " + i, new Date(), 3.4 + i);
            p.setCreateDate(new Date());
            pets.add(p);
        }
        ExcelCreator creator = new ExcelCreator();
        creator.createSheet();
        creator.fillEntityData(pets);
        File f = new File("D:\\datacenter\\1.xls");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            creator.write(out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 快捷静态方法导出
        // try( FileOutputStream out =new FileOutputStream("e:\\1.xls")) {
        // ExcelCreator.export(pets, out);
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    /**
     * map数据导出
     */
    public void testExportA() {
        List<LinkedHashMap<String,Object>> pets = new ArrayList<>(300);
        for (int i = 0; i < 300; i++) {
            LinkedHashMap<String,Object> m = new LinkedHashMap<String,Object>();
            m.put("age", 30 + i);
            m.put("name", "唯唯诺诺" + i);
            m.put("birthday", new Date());
            m.put("skin", "白皮肤");
            m.put("salary", new BigDecimal("322" + i));
            pets.add(m);
        }
        File f = new File("e:\\2.xls");
        try {
            if (!f.exists()) {
                f.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(f);
            ExcelCreator.export(pets, new String[]{ "name","age","skin","salary","birthday","abc" }, out);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 导入数据转为实体对象
     * 
     * @throws IOException
     */
    @org.junit.Test
    public void testImport() throws IOException {
        FileInputStream in = new FileInputStream("e:\\documents\\1.xlsx");
        Workbook wb = new XSSFWorkbook(in);// HSSFWorkbook
        in.close();
        ExcelImportor xlsImport = new ExcelImportor();
        xlsImport.setStepValid(false);
        xlsImport.setStartRowIndex(1);
        try {
            LinkedHashSet<Pet> results = xlsImport.importSheet(wb.getSheetAt(0), Pet.class);
            for (Pet pet : results) {
                System.out.println(pet.getName() + ",age:" + pet.getAge() + ",skin:" + pet.getSkin() + ",money:"
                        + pet.getMoney() + ",birthday:" + pet.getBirthday());
            }

        } catch (ExcelAccessException e) {
            e.printStackTrace();
            Map<Integer,String> xMap = xlsImport.getErrorMap();
            System.out.println(xMap.values().toString());
        }
        // List<Integer> resultIndexs=xlsImport.getCurRowIndexs();
    }

}
