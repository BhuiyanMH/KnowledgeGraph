import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GenerateABox {

    //define the file paths
    static final String ARTICLE= "src/main/resources/article.csv";
    static final String ARTICLE_KEYWORD= "src/main/resources/article_keyword.csv";
    static final String AUTHOR_ORG= "src/main/resources/auth_org.csv";
    static final String AUTHOR_ARTICLE= "src/main/resources/author_article.csv";
    static final String AUTHOR= "src/main/resources/authors.csv";
    static final String CITATION= "src/main/resources/citation.csv";
    static final String CONFERENCE= "src/main/resources/conference.csv";
    static final String CONFERENCE_ARTICLE= "src/main/resources/conference_article.csv";
    static final String JOURNAL= "src/main/resources/journal.csv";
    static final String JOURNAL_ARTICLE= "src/main/resources/journal_article.csv";
    static final String KEYWORD= "src/main/resources/keywords.csv";
    static final String ORGANIZATION= "src/main/resources/organization.csv";
    static final String REVIEW= "src/main/resources/review.csv";
    static final String ABOX_PATH = "src/main/resources/abox/";

    //public static final String BASE_URL = "http://www.semanticweb.org/";
    public static final String BASE_URL = "http://www.example.com/publication.owl/";

//    public static final String PROPERTY_URL = BASE_URL+"property/";
//    public static final String RESOURCE_URL = BASE_URL+"resource/";

    public static void main(String args[]) {

        Model aboxModel = ModelFactory.createDefaultModel();
        //aboxModel = ModelFactory.createUnion(aboxModel, generateAuthorTriples());
        //aboxModel = ModelFactory.createUnion(aboxModel, generateKeywordTriples());
        //aboxModel = ModelFactory.createUnion(aboxModel, generateJournalTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateJournalArticleTriples());


        //writeTriples(aboxModel, ABOX_PATH+"abox.nt");

    }

    static Model generateJournalArticleTriples() {

        ArrayList<String> rows = readCSV(JOURNAL_ARTICLE);
//        System.out.println(rows.get(0));
//        //id;title;Journal;ee;year;volume;pages
//        System.out.println(rows.get(1));
//        //200;A data base system for river basin management.;0;http://dl.acm.org/citation.cfm?id=811243;2016;1;331-340

        rows.remove(0); //Remove the header
        HashMap journalNameMap = getColumnValues(0, 1, JOURNAL);

        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {
            //System.out.println(row);
            String[] columns = row.split(";");

            int id = Integer.parseInt(columns[0].trim());
            String name = columns[1].trim();
            String journalID = columns[2].trim();
            String ee = columns[3].trim();
            int year = Integer.parseInt(columns[4].trim());
            int volume = Integer.parseInt(columns[5].trim());
            String pages = columns[6]

            String journalURI = BASE_URL+cleanString(name);

            Resource keyword = model.createResource(journalURI)
                    .addProperty(model.createProperty(BASE_URL+"journalTitle"),name)
                    .addProperty(model.createProperty(BASE_URL+"issn"),issn)
                    .addProperty(model.createProperty(BASE_URL+"publisher"),publisher);

            model.addLiteral(keyword, model.createProperty(BASE_URL+"journalID"), id);

        }
        writeTriples(model, ABOX_PATH+"journal_article.nt");

        return model;
    }


    static Model generateJournalTriples() {

        ArrayList<String> rows = readCSV(JOURNAL);
        //System.out.println(rows.get(0));
        //id;name;issn;publisher
        rows.remove(0); //Remove the header
        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {
            //System.out.println(row);
            String[] columns = row.split(";");

            int id = Integer.parseInt(columns[0].trim());
            String name = columns[1].trim();
            String issn = columns[2].trim();
            String publisher  = columns[3].trim();

            String journalURI = BASE_URL+cleanString(name);

            Resource keyword = model.createResource(journalURI)
                    .addProperty(model.createProperty(BASE_URL+"journalTitle"),name)
                    .addProperty(model.createProperty(BASE_URL+"issn"),issn)
                    .addProperty(model.createProperty(BASE_URL+"publisher"),publisher);

            model.addLiteral(keyword, model.createProperty(BASE_URL+"journalID"), id);

        }
        writeTriples(model, ABOX_PATH+"journal.nt");

        return model;
    }
    static Model generateKeywordTriples() {

        ArrayList<String> rows = readCSV(KEYWORD);
        //System.out.println(rows.get(0));
        rows.remove(0); //Remove the header
        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {
            //System.out.println(row);
            String[] columns = row.split(";");

            int id = Integer.parseInt(columns[0].trim());
            String name = columns[1].trim();
            String keywordURI = BASE_URL+cleanString(name);

            Resource keyword = model.createResource(keywordURI)
                    .addProperty(model.createProperty(BASE_URL+"keyName"),name);
            model.addLiteral(keyword, model.createProperty(BASE_URL+"keyID"), id);

        }
        writeTriples(model, ABOX_PATH+"keyword.nt");

        return model;
    }
    static Model generateAuthorTriples() {

        ArrayList<String> rows = readCSV(AUTHOR);
        rows.remove(0); //Remove the header
        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {
            //System.out.println(row);
            String[] columns = row.split(";");

            int id = Integer.parseInt(columns[0].trim());
            String name = columns[1].trim();
            String authorURI = BASE_URL+cleanString(name);

            Resource author = model.createResource(authorURI)
                    .addProperty(model.createProperty(BASE_URL+"authorName"),name);
            model.addLiteral(author, model.createProperty(BASE_URL+"authorID"), id);

        }
        writeTriples(model, ABOX_PATH+"author.nt");

        return model;
    }

    static void printHashMap(HashMap map){

        for (Object name: map.keySet()){
            String key = name.toString();
            String value = map.get(name).toString();
            System.out.println(key + " " + value);
        }
    }
    static HashMap getColumnValues(int columnToMatch, int returnColumn, String filePath){

        HashMap<String, String> valueMap = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(filePath));
            String row;

            while ((row = reader.readLine()) != null) {

                String[] columns = row.split(";");

                String key = columns[columnToMatch];
                String value = columns[returnColumn];

                valueMap.put(key, value);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File "+filePath+ " not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Reading file "+ filePath + " failed!");
            e.printStackTrace();
        }finally {
            return valueMap;
        }

    }
    static void writeTriples(Model model, String filePath){
        try {
            model.write(new BufferedOutputStream(
                            new FileOutputStream(filePath)), "NT");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
    static String cleanString(String string){
        String cleanString = StringUtils.stripAccents(string);
        cleanString = cleanString.replace(".", "");
        cleanString = cleanString.replace("-", "_");
        cleanString = cleanString.replace(" ", "_");
        return cleanString;
    }
    static ArrayList<String> readCSV(String filePath) {
        ArrayList<String> lines = new ArrayList<>();
        BufferedReader reader;
        try {
            reader = new BufferedReader(new FileReader(filePath));
            String row;
            while ((row = reader.readLine()) != null) {
                lines.add(row);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            System.out.println("File "+filePath+ " not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Reading file "+ filePath + " failed!");
            e.printStackTrace();
        }finally {

            return lines;
        }

    }
}