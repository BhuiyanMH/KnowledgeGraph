import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

public class GenerateABox {

    //define the file paths
    //static final String ARTICLE= "src/main/resources/article.csv";
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
        aboxModel = ModelFactory.createUnion(aboxModel, generateAuthorTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateKeywordTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateJournalTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateJournalArticleTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateConference());
        aboxModel = ModelFactory.createUnion(aboxModel, generateConferenceArticleTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateArticleKeywordTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateAuthorArticleTriples());
        aboxModel = ModelFactory.createUnion(aboxModel, generateCitationTriples());

        writeTriples(aboxModel, ABOX_PATH+"abox.nt");

    }

    static Model generateCitationTriples() {

        ArrayList<String> rows = readCSV(CITATION);
        rows.remove(0); //Remove the header

        HashMap articleNameMap = getAllArticleNames();

        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {

            //System.out.println(row);
            String[] columns = row.split(";");

            String sourcePaperID = columns[0].trim();
            String citedPaperID = columns[1].trim();

            String sourcePaperName = (String) articleNameMap.get(sourcePaperID);
            String citedPaperName = (String) articleNameMap.get(citedPaperID);

            String sourcePaperURI = BASE_URL + cleanString(sourcePaperName);
            String citedPaperURI = BASE_URL+cleanString(citedPaperName);

            Resource sourceArticle = model.createResource(sourcePaperURI);
            Resource citedArticle = model.createResource(citedPaperURI)
                    .addProperty(model.createProperty(BASE_URL+"citedBy"), sourceArticle);
        }
        writeTriples(model, ABOX_PATH+"citation.nt");
        return model;
    }

    static Model generateAuthorArticleTriples() {

        ArrayList<String> rows = readCSV(AUTHOR_ARTICLE);
        rows.remove(0); //Remove the header

        HashMap articleNameMap = getAllArticleNames();
        HashMap authorNameMap = getColumnValues(0, 1, AUTHOR);

        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {

            //System.out.println(row);
            String[] columns = row.split(";");

            String authorID = columns[0].trim();
            String articleID = columns[1].trim();

            String articleName = (String) articleNameMap.get(articleID);
            String authorName = (String) authorNameMap.get(authorID);

            String authorURI = BASE_URL + cleanString(authorName);
            String articleURI = BASE_URL+cleanString(articleName);

            Resource article = model.createResource(articleURI);
            Resource author = model.createResource(authorURI)
                    .addProperty(model.createProperty(BASE_URL+"written"), article);
        }
        writeTriples(model, ABOX_PATH+"author_article.nt");
        return model;
    }
    static Model generateArticleKeywordTriples() {

        ArrayList<String> rows = readCSV(ARTICLE_KEYWORD);

        rows.remove(0); //Remove the header
        HashMap articleNameMap = getAllArticleNames();
        HashMap keywordNameMap = getColumnValues(0, 1, KEYWORD);

        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {

            String[] columns = row.split(";");

            String paperID = columns[0].trim();
            String keywordID = columns[1].trim();

            String paperName = (String) articleNameMap.get(paperID);
            String keywordName = (String) keywordNameMap.get(keywordID);

            String paperURI = BASE_URL + cleanString(paperName);
            String keywordURI = BASE_URL+cleanString(keywordName);

            Resource keyword = model.createResource(keywordURI);
            Resource paper = model.createResource(paperURI)
                    .addProperty(model.createProperty(BASE_URL+"hasKeyword"), keyword);
        }
        writeTriples(model, ABOX_PATH+"article_keyword.nt");
        return model;
    }
    static Model generateConferenceArticleTriples() {

        ArrayList<String> rows = readCSV(CONFERENCE_ARTICLE);
        //System.out.println(rows.get(0));
        //id;title;conference;edition;ee;pages
        //System.out.println(rows.get(1));
        //0;MBase: Representing mathematical knowledge in a relational data base.;0;1;https://doi.org/10.1016/S1571-0661(05)80615-2|https://www.wikidata.org/entity/Q57389432;451-468


        rows.remove(0); //Remove the header
        HashMap conferenceNameMap = getColumnValues(0, 1, CONFERENCE);

        //printHashMap(conferenceNameMap);

        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {
            //System.out.println(row);
            String[] columns = row.split(";");

            //id;title;conference;edition;ee;pages
            int id = Integer.parseInt(columns[0].trim());
            String name = columns[1].trim();
            String confID = columns[2].trim();
            String ee = columns[4].trim();
            String pages = "";
            if(columns.length >= 6)
                pages = columns[5].trim();

            String confName = (String) conferenceNameMap.get(confID);
            String confURI = BASE_URL + cleanString(confName);

            String paperURI = BASE_URL+cleanString(name);
            //id;title;conference;edition;ee;pages

            Resource paper = model.createResource(paperURI)
                    .addProperty(model.createProperty(BASE_URL+"paperTitle"), name)
                    .addProperty(model.createProperty(BASE_URL+"ee"),ee)
                    .addProperty(model.createProperty(BASE_URL+"page"),pages)
                    .addProperty(model.createProperty(BASE_URL+"publishedIn"), model.createResource(confURI));

            model.addLiteral(paper, model.createProperty(BASE_URL+"paperID"), id);

        }
        writeTriples(model, ABOX_PATH+"conference_article.nt");

        return model;
    }
    static Model generateConference() {

        ArrayList<String> rows = readCSV(CONFERENCE);
        //System.out.println(rows.get(0));
        //id;name;edition;city;year
        //0;VLDB;1;Berlin;2016

        rows.remove(0); //Remove the header


        Model model = ModelFactory.createDefaultModel();

        for (String row:rows) {
            //System.out.println(row);
            String[] columns = row.split(";");

            int id = Integer.parseInt(columns[0].trim());
            String name = columns[1].trim();
            //int edition = Integer.parseInt(columns[2].trim());
            String city = columns[3].trim();
            //int year = Integer.parseInt(columns[4].trim());

            String confURI = BASE_URL+cleanString(name);
            String cityURI = BASE_URL+cleanString(city);
            //id;name;edition;city;year

            Resource cityNode = model.createResource(cityURI)
                    .addProperty(model.createProperty(BASE_URL+"cityName"), city);

            Resource conference = model.createResource(confURI)
                    .addProperty(model.createProperty(BASE_URL+"confTitle"), name)
                    .addProperty(model.createProperty(BASE_URL+"heldIn"), cityNode);

            model.addLiteral(conference, model.createProperty(BASE_URL+"confID"), id);

        }
        writeTriples(model, ABOX_PATH+"conference.nt");

        return model;
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
            String pages = "";

            if(columns.length >= 7) //some entries without pages
                pages = columns[6];

            String journalName = (String) journalNameMap.get(journalID);
            String journalURI = BASE_URL + cleanString(journalName);

            String paperURI = BASE_URL+cleanString(name);
            //        //id;title;Journal;ee;year;volume;pages

            Resource paper = model.createResource(paperURI)
                    .addProperty(model.createProperty(BASE_URL+"paperTitle"), name)
                    .addProperty(model.createProperty(BASE_URL+"ee"),ee)
                    .addProperty(model.createProperty(BASE_URL+"page"),pages)
                    .addProperty(model.createProperty(BASE_URL+"publishedIn"), model.createResource(journalURI));

            model.addLiteral(paper, model.createProperty(BASE_URL+"paperID"), id);
            model.addLiteral(paper, model.createProperty(BASE_URL+"year"), year);
            model.addLiteral(paper, model.createProperty(BASE_URL+"volume"), volume);

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
    static HashMap getAllArticleNames(){

        HashMap<String, String> valueMap = new HashMap<>();
        BufferedReader reader;

        try {
            reader = new BufferedReader(new FileReader(CONFERENCE_ARTICLE));
            String row;

            while ((row = reader.readLine()) != null) {

                String[] columns = row.split(";");

                String key = columns[0];
                String value = columns[1];

                valueMap.put(key, value);
            }

            reader = new BufferedReader(new FileReader(JOURNAL_ARTICLE));

            while ((row = reader.readLine()) != null) {

                String[] columns = row.split(";");

                String key = columns[0];
                String value = columns[1];

                valueMap.put(key, value);
            }

            reader.close();

        } catch (FileNotFoundException e) {
            System.out.println("File not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("Reading file failed!");
            e.printStackTrace();
        }finally {
            return valueMap;
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
        cleanString = cleanString.replaceAll("\\p{Punct}", "");
        cleanString = cleanString.replace(".", "");
        cleanString = cleanString.replace("-", "_");
        cleanString = cleanString.replace(" ", "_");
        cleanString = cleanString.replace("'", "");
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