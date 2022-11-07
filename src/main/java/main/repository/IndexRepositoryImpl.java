package main.repository;

import lombok.extern.slf4j.Slf4j;
import main.config.Props;
import main.model.Index;
import main.model.Site;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.List;

@Slf4j
@Repository
@Transactional
public class IndexRepositoryImpl implements IndexRepositoryCustom {
    @Autowired
    EntityManager entityManager;
    public static final String TABS = "\t\t";

    @Override
    public void insertIndexList(String siteName, List<Index> indices) {
        StringBuilder insertBuilder = new StringBuilder();
        int currIndex = 0;
        while (currIndex < indices.size()) {
            currIndex = buildInserts(indices, insertBuilder, currIndex);

            String sql = Props.getInst().getMultiInsertString() +
                    " values " + insertBuilder;
            insertBuilder.setLength(0);
            Query query = entityManager.
                    createNativeQuery(sql);
            query.executeUpdate();
            log.info(TABS + "Сайт \"" + siteName + "\": " +
                    "сохранено " + currIndex + " индексов");
        }
    }

    private int buildInserts(List<Index> indices, StringBuilder insertBuilder, int currIndex) {
        for (int i = 0; i++ < 100_000 && currIndex < indices.size(); currIndex++) {
            Index index = indices.get(currIndex);
            insertBuilder.append(insertBuilder.length() == 0 ? "" : ",")
                    .append("(").append(index.getPage().getId())
                    .append(",").append(index.getLemma().getId())
                    .append(",").append(index.getRank()).append(")");
        }
        return currIndex;
    }
}
