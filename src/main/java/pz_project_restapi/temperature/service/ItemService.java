package pz_project_restapi.temperature.service;

import java.time.*;
import java.time.format.*;
import java.util.*;
import org.springframework.stereotype.*;
import pz_project_restapi.temperature.model.*;
import pz_project_restapi.temperature.repository.*;



@Component
public class ItemService implements IItemService{

    private ItemRepository storage;

    public ItemService(ItemRepository storage) {
        this.storage = storage;
    }

    private List<Item> storageData = new ArrayList<>();
    private TemperatureForm temperatureForm = new TemperatureForm();

    // final GET longest period by temperature
    public List<Item> getPeriodTe() {
        List<Item> temporaryList = getAllItemsFromDatabase();
        List<ItemLDT> temporaryListLDT = convertToLocalDateTime(temporaryList);
        sortLocalDateTime(temporaryListLDT);
        float A = temperatureForm.getTemperatureA();
        float B = temperatureForm.getTemperatureB();
        List<ItemLDT> periodListLDT = longestPeriodByTemperature(temporaryListLDT, A, B);
        List<Item> periodList = convertToString(periodListLDT);
        return periodList;
    }

    // GET longest period by termperature and time
    public List<Item> getPeriodTeTi() {
        List<Item> temporaryList = getAllItemsFromDatabase();
        List<ItemLDT> temporaryListLDT = convertToLocalDateTime(temporaryList);
        sortLocalDateTime(temporaryListLDT);
        float A = temperatureForm.getTemperatureA();
        float B = temperatureForm.getTemperatureB();
        LocalTime X = convertToLocalTime(temperatureForm.getTimeX());
        LocalTime Y = convertToLocalTime(temperatureForm.getTimeY());
        List<ItemLDT> periodListLDT = longestPeriodByTemperatureAndTime(temporaryListLDT, A, B, X, Y);
        List<Item> periodList = convertToString(periodListLDT);
        return periodList;
    }

    //download and save all data from databases for methods longest period
    public synchronized List<Item> getAllItemsFromDatabase(){
        List<Item> temporaryStorage = storage.findAll();
        for (Item item : temporaryStorage) {
            storageData.add(item);
        }
        return storageData;
    }

    //convert object Item String to object ItemLDT LocalDateTime
    private static List<ItemLDT> convertToLocalDateTime(List<Item> item) {
        List<ItemLDT> temporaryStorageLocalDateTime = new ArrayList<>();

        for (int i = 0; i < item.size(); i++) {
            String s = item.get(i).getDateAndTime();
            Long tempId = item.get(i).getId();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime localDateTime = LocalDateTime.parse(s, formatter);
            float f = item.get(i).getTemperature();
            ItemLDT tempItemLDT = new ItemLDT(tempId, localDateTime, f);
            temporaryStorageLocalDateTime.add(tempItemLDT);
        }
        return temporaryStorageLocalDateTime;
    }

    //sort List of object ItemLDT LocalDateTime
    private static List<ItemLDT> sortLocalDateTime(List<ItemLDT> itemLDT) {
        itemLDT.sort(Comparator.comparing(ItemLDT::getLocalDateTime));
        return itemLDT;
    }

    //conveert object LocalDate to object String
    private static List<Item> convertToString(List<ItemLDT> itemLDT) {
        List<Item> temporaryStringStorage = new ArrayList<>();
        for (int i = 0; i < itemLDT.size() ; i++) {
            LocalDateTime ldt = itemLDT.get(i).getLocalDateTime();
            Long tempId = itemLDT.get(i).getId();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            String formattedDateTime = ldt.format(formatter);
            float f = itemLDT.get(i).getTemperatureLDT();
            Item tempItem = new Item(tempId, formattedDateTime, f);
            temporaryStringStorage.add(tempItem);
        }
        return temporaryStringStorage;
    }

    // save temperature limits
    public TemperatureForm saveTemperatureLimits(TemperatureForm newLimits) {
        temperatureForm.setTemperatureA(newLimits.getTemperatureA());
        temperatureForm.setTemperatureB(newLimits.getTemperatureB());
        return temperatureForm;
    }

    //save temperature and time limits
    public TemperatureForm saveTemperatureAndTimeLimits(TemperatureForm newLimits) {
        temperatureForm.setTemperatureA(newLimits.getTemperatureA());
        temperatureForm.setTemperatureB(newLimits.getTemperatureB());
        temperatureForm.setTimeX(newLimits.getTimeX());
        temperatureForm.setTimeY(newLimits.getTimeY());
        return temperatureForm;
    }

    //convert String to LocalTime
    private static LocalTime convertToLocalTime (String time) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime localTime = LocalTime.parse(time, dtf);
        return localTime;
    }

    private static List<Tuple> longestEqualSeq(List<Integer> values) {
        int theCount = 0;
        int theIdx = 0;
        int count = 1;      
        List<Tuple> out = new ArrayList<Tuple>();
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i - 1).equals(1) && (values.get(i).equals(1))) {
                count++;
                if (theCount < count) {
                    theCount = count;
                    theIdx = i;
                }
            } else {
                if (count > 1) {
                    out.add(new Tuple(theIdx - (theCount - 1), theCount));
                }
                count = 1;
                theCount = 0;
            }
        }
        if (count > 1) {
            out.add(new Tuple(theIdx - (theCount - 1), theCount));
        }
        out.sort(Comparator.comparing(Tuple::getLength).reversed());
        return out;
    }

    //longest period in days, where temperature is between A and B
    public List<ItemLDT> longestPeriodByTemperature(List<ItemLDT> itemLDT,
                                                    float temperatureA, float temperatureB) {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < itemLDT.size(); i++) {
            if (((itemLDT.get(i).getTemperatureLDT() >= temperatureA) &&
                    (itemLDT.get(i).getTemperatureLDT() <= temperatureB))) {
                values.add(1);
            } else {
                values.add(0);
            }
        }
        List<Tuple> seqs = ItemService.longestEqualSeq(values);
        int theCount = seqs.get(seqs.size()-1).getLength();
        int theIdx = seqs.get(0).getStart();
        List<ItemLDT> finalPeriod = new ArrayList<>();
        for (int i = theIdx; i < (theCount + theIdx); i++) {
            finalPeriod.add(new ItemLDT(itemLDT.get(i).getId(), itemLDT.get(i).getLocalDateTime(),
                    itemLDT.get(i).getTemperatureLDT()));
        }
        List<ItemLDT> startEndPeriod = new ArrayList<>();
        startEndPeriod.add(finalPeriod.get(0));
        startEndPeriod.add(finalPeriod.get(finalPeriod.size()-1));
        return startEndPeriod;
    }

    //lond period in days, where tempereature is between A nad B and also
    // it was in interval between X and Y
    public List<ItemLDT> longestPeriodByTemperatureAndTime
    (List<ItemLDT> itemLDT, float temperatureA, float temperatureB,
     LocalTime timeX, LocalTime timeY) {
        List<ItemLDT> periodByTemperatureTime = new ArrayList<>();
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < itemLDT.size(); i++) {
            float tempTemperature = itemLDT.get(i).getTemperatureLDT();
            LocalDateTime dateTime = itemLDT.get(i).getLocalDateTime();
            LocalTime time = dateTime.toLocalTime();
            if(((tempTemperature >= temperatureA)&& (tempTemperature <= temperatureB)) &&
                    ((time.equals(timeX) || time.isAfter(timeX)) &&
                            ((time.equals(timeY) || time.isBefore(timeY)))))  {
                values.add(1);
            } else {
                values.add(0);
            }
        }
        List<Tuple> seqs = ItemService.longestEqualSeq(values);
        int theCount = seqs.get(seqs.size()-1).getLength();
        int theIdx = seqs.get(0).getStart();
        List<ItemLDT> finalPeriod = new ArrayList<>();
        for (int i = theIdx; i <= (theCount + theIdx); i++) {
            finalPeriod.add(new ItemLDT(itemLDT.get(i).getId(), itemLDT.get(i).getLocalDateTime(),
                    itemLDT.get(i).getTemperatureLDT()));
        }
        List<ItemLDT> startEndPeriod = new ArrayList<>();
        startEndPeriod.add(finalPeriod.get(0));
        startEndPeriod.add(finalPeriod.get(finalPeriod.size()-1));
        return startEndPeriod;
    }
}
