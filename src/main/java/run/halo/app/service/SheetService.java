package run.halo.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import run.halo.app.model.dto.post.SheetDetailDTO;
import run.halo.app.model.dto.post.SheetListDTO;
import run.halo.app.model.entity.Sheet;
import run.halo.app.model.enums.PostStatus;
import run.halo.app.service.base.CrudService;

/**
 * Sheet service interface.
 *
 * @author johnniang
 * @date 19-4-24
 */
public interface SheetService extends CrudService<Sheet, Integer> {

    /**
     * Creates a sheet.
     *
     * @param sheet sheet must not be null
     * @return created sheet
     */
    @NonNull
    Sheet createBy(@NonNull Sheet sheet);

    /**
     * Updates a sheet.
     *
     * @param sheet sheet must not be null
     * @return updated sheet
     */
    @NonNull
    Sheet updateBy(@NonNull Sheet sheet);

    /**
     * Gets a page of sheet.
     *
     * @param pageable page info must not be null
     * @return a page of sheet
     */
    @NonNull
    Page<Sheet> pageBy(@NonNull Pageable pageable);

    /**
     * Gets sheet by post status and url.
     *
     * @param status post status must not be null
     * @param url    sheet url must not be blank
     * @return sheet info
     */
    @NonNull
    Sheet getBy(@NonNull PostStatus status, @NonNull String url);


    /**
     * Converts to detail dto.
     *
     * @param sheet sheet must not be null
     * @return sheet detail dto
     */
    @NonNull
    SheetDetailDTO convertToDetailDto(@NonNull Sheet sheet);


    /**
     * Converts to list dto page.
     *
     * @param sheetPage sheet page must not be nulls
     * @return a page of sheet list dto
     */
    @NonNull
    Page<SheetListDTO> convertToListDto(@NonNull Page<Sheet> sheetPage);


}
