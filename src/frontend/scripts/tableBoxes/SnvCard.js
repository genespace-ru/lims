import React from 'react';
import { registerTableBox, Navs } from 'be5-react';

export default function SnvCard({ value }) 
{
    const groups = value.data.attributes.rows;

    let steps = [];
    for (const group of groups) 
    {
        let view = group.viewname.value;
        if( ! view )
            view = "SNV tab";
         
        let step = { title: group.title_ru.value,
                     url: "#!table/snv_/" + view + "/snvID=" + group.snvID.value + "/groupID=" + group.id.value }; 

        steps.push(step);
    }    

  return (
    <div className="SnvCard">
      <Navs steps={steps} tabs startAtStep={0} />
    </div>
  );
};

registerTableBox('SnvCard', SnvCard);
