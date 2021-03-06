package org.jflame.toolkit.excel;

import java.beans.PropertyDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jflame.toolkit.excel.convertor.ExcelConvertorSupport;
import org.jflame.toolkit.excel.convertor.ICellValueConvertor;
import org.jflame.toolkit.excel.handler.ArraySheetRowHandler;
import org.jflame.toolkit.excel.handler.BaseEntitySheetRowHandler;
import org.jflame.toolkit.excel.handler.DefaultEntitySheetRowHandler;
import org.jflame.toolkit.excel.validator.DefaultValidator;
import org.jflame.toolkit.excel.validator.IValidator;
import org.jflame.toolkit.file.FileHelper;
import org.jflame.toolkit.reflect.BeanHelper;
import org.jflame.toolkit.util.CollectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * excel数据导入工具类 ，导入数据转为对应实体类集合或Object[]集体.导入过程可指定数据验证规则
 * <p>
 * 注:单独格式转换需使用XlsConvertorHolder类注册. 由于set是根据equals去重,所以想要导入重复数据不要覆盖equals <br>
 * 示例:
 * 
 * <pre>
 * 
 * ExcelImportor xlsImport = new ExcelImportor();
 * xlsImport.setStepValid(false);
 * xlsImport.setStartRowIndex(1);
 * try {
 *     LinkedHashSet&lt;Pet&gt; results = xlsImport.importSheet(sheet1, Pet.class);
 * } catch (ExcelAccessException e) {
 *     xlsImport.getErrorMap();// 错误信息
 * }
 * List&lt;Integer&gt; resultIndexs = xlsImport.getCurRowIndexs();
 * 
 * </pre>
 * 
 * @see ExcelColumn
 * @see ExcelConvertorSupport#registerConvertor(ICellValueConvertor...)
 * @author zyc
 */
public class ExcelImportor {

    private final Logger log = LoggerFactory.getLogger(ExcelImportor.class);
    private boolean stepValid = true;// 是否执行单行验证,验证失败即中断
    private boolean ignoreRepeat = true;// 忽略重复数据,重复只导入一行.如果不忽略,发现重复数据时将停止导入
    private int startRowIndex = 1;
    private Map<Integer,String> errorMap = new HashMap<>();
    private List<Integer> curRowIndexs = new ArrayList<Integer>();
    private ExcelAnnotationResolver annotResolver = new ExcelAnnotationResolver();
    private Workbook workbook;

    public ExcelImportor() {
    }

    /**
     * 构造函数
     * 
     * @param excelFile 指定excel文件
     * @throws ExcelAccessException
     */
    public ExcelImportor(String excelFile) throws ExcelAccessException {
        try (FileInputStream xlsStream = new FileInputStream(excelFile)) {
            if ("xls".equals(FileHelper.getExtension(excelFile, false))) {
                workbook = new HSSFWorkbook(xlsStream);
            } else {
                workbook = new XSSFWorkbook(xlsStream);
            }
        } catch (IOException e) {
            throw new ExcelAccessException("未能读取文件,或文件格式损坏," + excelFile);
        }
    }

    /**
     * 取得某行在excel中对应的行索引.
     * 
     * @param index 导入数据集中的索引
     * @return excel中行号
     */
    public Integer getRowIndex(int index) {
        return curRowIndexs.get(index);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合.从第一张sheet导入
     * 
     * @param dataClass
     * @return
     */
    public <T extends IExcelEntity> LinkedHashSet<T> importSheet(final Class<T> dataClass) {
        return importSheet(workbook.getSheetAt(0), dataClass, null, null);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合.从第一张sheet导入
     * 
     * @param dataClass
     * @param validator 自定义验证器
     * @return
     */
    public <T extends IExcelEntity> LinkedHashSet<T> importSheet(final Class<T> dataClass, IValidator<T> validator) {
        return importSheet(workbook.getSheetAt(0), dataClass, validator, null);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合. 要转换属性及顺序由dataClass的excelColumn注解决定. <br>
     * 使用默认的验证器验证.使用默认单行数据转换器
     * 
     * @param sheet 工作表
     * @param dataClass 转换类型
     * @param <T> dataClass 泛型类型
     * @exception ExcelAccessException
     * @return 返回为LinkedHashSet类型的数据集
     */
    public <T extends IExcelEntity> LinkedHashSet<T> importSheet(Sheet sheet, final Class<T> dataClass) {
        return importSheet(sheet, dataClass, null, null);
    }

    /**
     * 导入指定的excel工作表数据,并转换为相应的对象集合. 要转换属性及顺序由dataClass的excelColumn注解决定. <br>
     * 使用自定义验证器验证.使用默认单行数据转换器
     * 
     * @param sheet 工作表
     * @param dataClass 转换类型
     * @param <T> dataClass泛型类型
     * @param validator 验证规则类
     * @exception ExcelAccessException
     * @return 返回为LinkedHashSet类型的数据集
     */
    public <T extends IExcelEntity> LinkedHashSet<T> importSheet(Sheet sheet, final Class<T> dataClass,
            IValidator<T> validator) {
        return importSheet(sheet, dataClass, validator, null);
    }

    /**
     * 导入excel工作表数据转换为指定类型的对象集合.
     * 
     * @param xsheet 工作表,null时导入第一张sheet
     * @param dataClass 转换类型class
     * @param <T> dataClass泛型类型
     * @param validator 指定数据验证类,为null使用DefaultValidator验证
     * @param sheetRowHandler 自定义单行数据转换器,缺省使用{@link DefaultEntitySheetRowHandler}
     * @see DefaultValidator
     * @exception ExcelAccessException
     * @return 返回为LinkedHashSet类型的不重复元素数据集
     */
    public <T extends IExcelEntity> LinkedHashSet<T> importSheet(Sheet xsheet, final Class<T> dataClass,
            IValidator<T> validator, final BaseEntitySheetRowHandler<T> sheetRowHandler) {
        if (dataClass == null) {
            throw new IllegalArgumentException("参数dataClass不能为null");
        }
        if (validator == null) {
            validator = new DefaultValidator<T>();
        }
        Sheet sheet = xsheet == null ? workbook.getSheetAt(0) : xsheet;
        BaseEntitySheetRowHandler<T> rowHandler = null;
        LinkedHashSet<T> results = new LinkedHashSet<>();
        curRowIndexs.clear();
        errorMap.clear();
        if (sheetRowHandler == null) {
            PropertyDescriptor[] properties = BeanHelper.getPropertyDescriptors(dataClass);
            if (properties == null) {
                throw new ExcelAccessException("bean属性内省异常,类名:" + dataClass.getName());
            }
            List<ExcelColumnProperty> lstDescriptors = annotResolver.getColumnPropertysByAnnons(properties);

            if (CollectionHelper.isEmpty(lstDescriptors)) {
                throw new ExcelAccessException("没有找到要转换的属性");
            }
            rowHandler = new DefaultEntitySheetRowHandler<>(lstDescriptors, dataClass);
        } else {
            rowHandler = sheetRowHandler;
        }

        Row curRow;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            curRow = rowIterator.next();
            if (curRow.getRowNum() < startRowIndex) {
                continue;
            }
            T newObj = rowHandler.extractRow(curRow);
            // 单行验证
            if (stepValid) {
                if (!validator.valid(newObj, curRow.getRowNum())) {
                    errorMap = validator.getErrors();
                    throw new ExcelAccessException("数据验证失败");
                }
            }
            if (results.add(newObj)) {
                curRowIndexs.add(curRow.getRowNum());
            } else {
                if (ignoreRepeat) {
                    log.warn("重复数据,忽略不导入.行数:{},对象:{}", (curRow.getRowNum() + 1), newObj);
                } else {
                    errorMap.put(curRow.getRowNum(), "重复的数据");
                    log.error("重复数据停止导入,行数:{},对象:{}", (curRow.getRowNum() + 1), newObj);
                    throw new ExcelAccessException("重复数据");
                }
            }
        }
        // 整体验证
        if (!stepValid && !results.isEmpty()) {
            Map<Integer,T> validMap = new LinkedHashMap<>();
            Iterator<T> iterator = results.iterator();
            int s = 0;
            while (iterator.hasNext()) {
                validMap.put(curRowIndexs.get(s), iterator.next());
                s++;
            }
            if (!validator.validList(validMap)) {
                errorMap = validator.getErrors();
                results = null;
                curRowIndexs.clear();
                throw new ExcelAccessException("数据验证失败");
            }
        }
        return results;
    }

    /**
     * 导入指定的excel工作表数据,转换为数组列表 数组元素类型只可能是bool,string类型.是否为bool,double类型由单元格类型决定.
     * 
     * @param sheet excel sheet
     * @return Object[]列表
     */
    public List<Object[]> importSheet(final Sheet sheet) {
        List<Object[]> results = new ArrayList<>();
        ArraySheetRowHandler rowHandler = new ArraySheetRowHandler();
        Row curRow;
        Iterator<Row> rowIterator = sheet.rowIterator();
        while (rowIterator.hasNext()) {
            curRow = rowIterator.next();
            if (curRow.getRowNum() < startRowIndex) {
                continue;
            }
            results.add(rowHandler.extractRow(curRow));
        }
        return results;
    }

    /*  private Object getCellValue(Cell curCell) {
        if (curCell.getCellType() == Cell.CELL_TYPE_BOOLEAN) {
            return curCell.getBooleanCellValue();
        } else if (curCell.getCellType() == Cell.CELL_TYPE_NUMERIC) {
            if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(curCell)) {
                return curCell.getDateCellValue();
            } else {
                return curCell.getNumericCellValue();
            }
        } else if (curCell.getCellType() == Cell.CELL_TYPE_FORMULA) {
            return curCell.getNumericCellValue();// 公式类型取计算结果
        } else {
            return curCell.getStringCellValue();
        }
    }*/

    /*private List<ColumnProperty> getColumnDescriptorsByName(final PropertyDescriptor[] properties,
            final String[] propertyNames) {
        List<ColumnProperty> lstDescriptors = new ArrayList<>(propertyNames.length);
        PropertyDescriptor tmProperty;
        ExcelColumn ec;
        for (String pname : propertyNames) {
            tmProperty = getPropertyDescriptorByName(properties, pname);
            if (tmProperty != null) {
                ColumnProperty cp = new ColumnProperty();
                cp.propertyDescriptor = tmProperty;
                ec = tmProperty.getReadMethod().getAnnotation(ExcelColumn.class);
                if (ec != null) {
                    cp.convertor = ec.convert();
                    cp.fmt = ec.fmt();
                    cp.columnName = ec.name();
                }
                lstDescriptors.add(cp);
            }
        }
        return lstDescriptors;
    }*/

    /*private PropertyDescriptor getPropertyDescriptorByName(PropertyDescriptor[] properties, String propertyName) {
        for (PropertyDescriptor pd : properties) {
            if (propertyName.equals(pd.getName())) {
                return pd;
            }
        }
        return null;
    }*/

    /*private List<ColumnProperty> getColumnPropertysByAnnons(PropertyDescriptor[] properties) {
        List<ColumnProperty> as = new ArrayList<ColumnProperty>();
        Method methodGetX;
        final Class<ExcelColumn> clazz = ExcelColumn.class;
        ColumnProperty newProperty;
        ExcelColumn tmpAnns;
        for (PropertyDescriptor propertyDescriptor : properties) {
            methodGetX = propertyDescriptor.getReadMethod();
            if (methodGetX.isAnnotationPresent(clazz)) {
                tmpAnns = methodGetX.getAnnotation(clazz);
    
                newProperty = new ColumnProperty();
                newProperty.propertyDescriptor = propertyDescriptor;
                newProperty.columnName = tmpAnns.name();
                newProperty.convertor = tmpAnns.convert();
                newProperty.fmt = tmpAnns.fmt();
                newProperty.order = tmpAnns.order();
                as.add(newProperty);
            }
        }
        Collections.sort(as);
        return as;
    }*/

    // private final String convertErrTpl = "第{0}行{1},格式错误";

    /* private Object convertValue(ColumnProperty colProperty, Cell curCell) {
        Object newValue = null;
        Object cellValue = null;
        try {
            cellValue = getCellValue(curCell);
            Class<?> paramClass = colProperty.propertyDescriptor.getWriteMethod().getParameterTypes()[0];
            newValue = ExcelConvertorSupport.convertValueFromCellValue(colProperty.convertor, paramClass,
                    colProperty.fmt, cellValue);
        } catch (ConvertException e) {
            String t = StringUtils.isNotEmpty(colProperty.columnName) ? colProperty.columnName
                    : "列" + (curCell.getColumnIndex() + 1);
            throw new ExcelAccessException(MessageFormat.format(convertErrTpl, curCell.getRowIndex() + 1, t), e);
        }
    
        return newValue;
    }*/

    public boolean isStepValid() {
        return stepValid;
    }

    /**
     * 设置是否单行数据验证失败立刻返回,默认为true 为false,则所有excel数据转换为java对象后再执行验证.
     * 
     * @param stepValid 是否单行数据验证失败立刻返回
     */
    public void setStepValid(boolean stepValid) {
        this.stepValid = stepValid;
    }

    /**
     * 返回最近一次导入的数据对应的excel行索引集 即最后一次调用importSheet方法返回的数据集对应的excel行索引.
     * 
     * @return 最近一次导入的数据对应的excel行索引集
     */
    public List<Integer> getCurRowIndexs() {
        return curRowIndexs;
    }

    public int getStartRowIndex() {
        return startRowIndex;
    }

    /**
     * 设置从第几行开始导入,行索引从1开始.
     * 
     * @param startRowIndex 开始sheet行索引
     */
    public void setStartRowIndex(int startRowIndex) {
        this.startRowIndex = startRowIndex;
    }

    /**
     * 获取最近一次数据导入的验证结果.
     * 
     * @return 出错行索引与错误信息map
     */
    public Map<Integer,String> getErrorMap() {
        return errorMap;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setIgnoreRepeat(boolean ignoreRepeat) {
        this.ignoreRepeat = ignoreRepeat;
    }

    /*    private class ColumnProperty implements Comparable<ColumnProperty> {
    
        public PropertyDescriptor propertyDescriptor;
        public String convertor;
        public String fmt;
        public String columnName;
        public int order;
    
        public int compareTo(ColumnProperty obj) {
            return this.order - obj.order;
        }
    }*/
}
